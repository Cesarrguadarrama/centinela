package mx.centinela.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import mx.centinela.domain.model.Clabe;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/** REST + SSE surface against real infrastructure: triage flow, rule CRUD, metrics, live feed. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  // See TransactionFlowIntegrationTest for why the test image is pinned to 3.8.1
  @Container
  static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));

  @Container
  static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  static KafkaProducer<String, String> producer;

  @Autowired TestRestTemplate rest;
  @LocalServerPort int port;

  @DynamicPropertySource
  static void containerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @BeforeAll
  static void initProducer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", kafka.getBootstrapServers());
    producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
  }

  @AfterAll
  static void closeProducer() {
    producer.close();
  }

  @Test
  @SuppressWarnings("unchecked")
  void alertTriageLifecycleThroughTheApi() {
    String source = randomClabe();
    publishSubThreshold(source);

    // List with filters until the alert shows up (>=1: off-hours may co-fire on night CI runs)
    await()
        .atMost(Duration.ofSeconds(90))
        .until(() -> !itemsOf(rest.getForObject(alertsUrl(source), Map.class)).isEmpty());
    Map<String, Object> alert = itemsOf(rest.getForObject(alertsUrl(source), Map.class)).get(0);
    assertThat(alert.get("status")).isEqualTo("NEW");
    String alertId = (String) alert.get("id");

    // review -> REVIEWED
    Map<String, Object> reviewed =
        rest.postForObject(url("/api/alerts/" + alertId + "/review"), null, Map.class);
    assertThat(reviewed.get("status")).isEqualTo("REVIEWED");

    // reconsider -> FALSE_POSITIVE
    Map<String, Object> falsePositive =
        rest.postForObject(url("/api/alerts/" + alertId + "/false-positive"), null, Map.class);
    assertThat(falsePositive.get("status")).isEqualTo("FALSE_POSITIVE");

    // account drill-down reflects the activity
    Map<String, Object> summary =
        rest.getForObject(url("/api/accounts/" + source + "/summary"), Map.class);
    assertThat(((Number) summary.get("sentCount")).longValue()).isGreaterThanOrEqualTo(1);
    assertThat(((Number) summary.get("alertCount")).longValue()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @SuppressWarnings("unchecked")
  void ruleCrudRoundTrip() {
    Map<String, Object> draft =
        Map.of(
            "type",
            "VELOCITY",
            "name",
            "Velocity estricta (test) " + UUID.randomUUID(),
            "description",
            "regla de prueba",
            "enabled",
            false,
            "severity",
            "LOW",
            "weight",
            10,
            "params",
            Map.of("maxTransfers", 50, "windowMinutes", 10));

    ResponseEntity<Map> created = rest.postForEntity(url("/api/rules"), draft, Map.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String id = (String) created.getBody().get("id");

    Map<String, Object> update =
        Map.of(
            "type",
            "VELOCITY",
            "name",
            created.getBody().get("name"),
            "description",
            "ajustada",
            "enabled",
            false,
            "severity",
            "MEDIUM",
            "weight",
            22,
            "params",
            Map.of("maxTransfers", 40, "windowMinutes", 10));
    rest.put(url("/api/rules/" + id), update);

    Map<String, Object> fetched = rest.getForObject(url("/api/rules/" + id), Map.class);
    assertThat(fetched.get("weight")).isEqualTo(22);
    assertThat(fetched.get("severity")).isEqualTo("MEDIUM");

    List<Map<String, Object>> all = rest.getForObject(url("/api/rules"), List.class);
    assertThat(all.size()).isGreaterThanOrEqualTo(6); // 5 seeded + this one
  }

  @Test
  void rejectsInvalidRulePayload() {
    Map<String, Object> invalid =
        Map.of("type", "VELOCITY", "name", "", "severity", "LOW", "weight", 400);

    ResponseEntity<Map> response = rest.postForEntity(url("/api/rules"), invalid, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void exposesMetrics() {
    publishSubThreshold(randomClabe());
    await()
        .atMost(Duration.ofSeconds(90))
        .until(
            () ->
                ((Number)
                            rest.getForObject(url("/api/metrics/overview"), Map.class)
                                .get("totalTransactions"))
                        .longValue()
                    > 0);

    Map<String, Object> overview = rest.getForObject(url("/api/metrics/overview"), Map.class);
    assertThat(((Number) overview.get("totalAlerts")).longValue()).isGreaterThanOrEqualTo(1);

    List<?> perMinute =
        rest.getForObject(url("/api/metrics/transactions-per-minute?minutes=5"), List.class);
    assertThat(perMinute).hasSize(5);

    ResponseEntity<List> distribution =
        rest.getForEntity(url("/api/metrics/score-distribution"), List.class);
    assertThat(distribution.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void streamsAlertsOverSse() throws Exception {
    CompletableFuture<String> firstAlertData = new CompletableFuture<>();
    Thread reader =
        new Thread(
            () -> {
              try {
                HttpURLConnection conn =
                    (HttpURLConnection)
                        URI.create(url("/api/stream/alerts")).toURL().openConnection();
                conn.setReadTimeout(60_000);
                conn.setRequestProperty("Accept", "text/event-stream");
                try (BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                  String line;
                  while ((line = in.readLine()) != null && !firstAlertData.isDone()) {
                    if (line.startsWith("data:")) {
                      firstAlertData.complete(line.substring(5).trim());
                    }
                  }
                }
              } catch (Exception e) {
                firstAlertData.completeExceptionally(e);
              }
            },
            "sse-test-reader");
    reader.setDaemon(true);
    reader.start();

    // Publish until the subscriber (whose registration races with us) sees an event
    await()
        .atMost(Duration.ofSeconds(60))
        .pollInterval(Duration.ofSeconds(2))
        .until(
            () -> {
              if (!firstAlertData.isDone()) {
                publishSubThreshold(randomClabe());
              }
              return firstAlertData.isDone();
            });

    String data = firstAlertData.get(1, TimeUnit.SECONDS);
    assertThat(data).contains("\"severity\":\"HIGH\"").contains("explanation");
  }

  private void publishSubThreshold(String source) {
    String json =
        """
        {"id":"%s","sourceClabe":"%s","destinationClabe":"%s","amount":49999.00,\
        "currency":"MXN","concept":"Pago de prueba","timestamp":"%s"}"""
            .formatted(UUID.randomUUID(), source, randomClabe(), Instant.now());
    producer.send(new ProducerRecord<>("spei.transactions", source, json));
    producer.flush();
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> itemsOf(Map<?, ?> page) {
    return (List<Map<String, Object>>) page.get("items");
  }

  private String alertsUrl(String clabe) {
    return url("/api/alerts?clabe=" + clabe + "&status=NEW");
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  private String randomClabe() {
    long account = ThreadLocalRandom.current().nextLong(0, 100_000_000_000L);
    return Clabe.fromBaseDigits("012180" + "%011d".formatted(account)).value();
  }
}
