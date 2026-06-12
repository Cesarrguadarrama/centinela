package mx.centinela.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import mx.centinela.domain.model.Clabe;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * End-to-end detection pipeline test against real infrastructure: a JSON event published to Kafka
 * must end as a persisted transaction plus the expected alerts in Postgres. Events are raw JSON
 * strings on purpose — this validates the wire contract, not shared Java classes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransactionFlowIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  // 3.8.1, not 3.9.0: testcontainers' startup script breaks with the 3.9 image on macOS ARM
  // (testcontainers-java#9506). Runtime (compose) stays on 3.9.0 — only the test image is pinned.
  @Container
  static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));

  @Container
  static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  static KafkaProducer<String, String> producer;

  @Autowired JdbcTemplate jdbc;

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
  void persistsTransactionAndRaisesSubThresholdAlert() {
    String source = randomClabe();
    String txId = publish(source, randomClabe(), "49999.00", Instant.now());

    Map<String, Object> alert = awaitAlert(txId, "SUB_THRESHOLD_AMOUNT");

    assertThat(alert.get("severity")).isEqualTo("HIGH");
    assertThat(alert.get("status")).isEqualTo("NEW");
    assertThat((String) alert.get("explanation")).contains("$49,999.00").contains("umbral");

    Long persisted =
        jdbc.queryForObject(
            "SELECT count(*) FROM transactions WHERE id = ?::uuid", Long.class, txId);
    assertThat(persisted).isEqualTo(1);
  }

  @Test
  void raisesVelocityAlertAfterTooManyTransfersInWindow() {
    String burstSource = randomClabe();
    String lastTxId = null;
    for (int i = 0; i < 12; i++) {
      lastTxId = publish(burstSource, randomClabe(), "1000.00", Instant.now());
    }

    Map<String, Object> alert = awaitAlert(lastTxId, "VELOCITY");

    assertThat((String) alert.get("explanation"))
        .contains("transferencias salientes")
        .contains("5 minutos");
  }

  @Test
  void raisesSmurfingAlertAndCompositeScoreForFragmentedTransfers() {
    String antSource = randomClabe();
    String antDestination = randomClabe();
    String lastTxId = null;
    for (int i = 0; i < 16; i++) { // 16 x $4,000 = $64,000 in small pieces
      lastTxId = publish(antSource, antDestination, "4000.00", Instant.now());
    }

    Map<String, Object> alert = awaitAlert(lastTxId, "SMURFING");
    assertThat((String) alert.get("explanation")).contains("hormiga").contains("transferencias");

    // SMURFING (45) + VELOCITY (40) both fire on the 16-transfer burst
    Integer score =
        jdbc.queryForObject(
            "SELECT score FROM transactions WHERE id = ?::uuid", Integer.class, lastTxId);
    assertThat(score).isGreaterThanOrEqualTo(85);
  }

  @Test
  void raisesCriticalMuleAlertWhenFundsConvergeAndDisperse() {
    // Numbers track the V4-tuned thresholds: minIncoming=10, dispersalRatio=0.7
    String mule = randomClabe();
    for (int i = 0; i < 10; i++) { // $200,000 converging from 10 distinct senders
      publish(randomClabe(), mule, "20000.00", Instant.now());
    }
    // Ensure every deposit is consumed before dispersal starts (deposits land on the
    // senders' partitions, so cross-partition ordering is not guaranteed otherwise)
    await()
        .atMost(Duration.ofSeconds(60))
        .until(
            () ->
                jdbc.queryForObject(
                        "SELECT count(*) FROM transactions WHERE destination_clabe = ?",
                        Long.class,
                        mule)
                    == 10L);

    publish(mule, randomClabe(), "75000.00", Instant.now());
    String secondPayout = publish(mule, randomClabe(), "75000.00", Instant.now()); // 75% out

    Map<String, Object> alert = awaitAlert(secondPayout, "MULE_ACCOUNT");
    assertThat(alert.get("severity")).isEqualTo("CRITICAL");
    assertThat((String) alert.get("explanation"))
        .contains("10 depósitos")
        .contains("remitentes distintos")
        .contains("cuenta mula");
  }

  @Test
  void raisesOffHoursAlertForSmallHoursActivity() {
    // 09:30Z = 03:30 in America/Mexico_City — inside the 00:00-05:00 window
    Instant smallHours = Instant.parse("2026-06-12T09:30:00Z");
    String txId = publish(randomClabe(), randomClabe(), "1200.00", smallHours);

    Map<String, Object> alert = awaitAlert(txId, "OFF_HOURS");

    assertThat(alert.get("severity")).isEqualTo("MEDIUM");
    assertThat((String) alert.get("explanation")).contains("03:30");
  }

  private String publish(String source, String destination, String amount, Instant timestamp) {
    String txId = java.util.UUID.randomUUID().toString();
    String json =
        """
        {"id":"%s","sourceClabe":"%s","destinationClabe":"%s","amount":%s,\
        "currency":"MXN","concept":"Pago de prueba","timestamp":"%s"}"""
            .formatted(txId, source, destination, amount, timestamp);
    producer.send(new ProducerRecord<>("spei.transactions", source, json));
    producer.flush();
    return txId;
  }

  /** Awaits the alert raised by the rule whose seeded name matches the rule type. */
  private Map<String, Object> awaitAlert(String txId, String ruleType) {
    // Generous timeout: the first consumed event pays for partition assignment of the
    // freshly-started listener container, which can take tens of seconds on CI runners.
    await()
        .atMost(Duration.ofSeconds(90))
        .pollInterval(Duration.ofMillis(500))
        .until(
            () ->
                !jdbc.queryForList(
                        "SELECT a.* FROM alerts a JOIN rules r ON r.id = a.rule_id "
                            + "WHERE a.transaction_id = ?::uuid AND r.type = ?",
                        txId,
                        ruleType)
                    .isEmpty());
    return jdbc.queryForList(
            "SELECT a.* FROM alerts a JOIN rules r ON r.id = a.rule_id "
                + "WHERE a.transaction_id = ?::uuid AND r.type = ?",
            txId,
            ruleType)
        .get(0);
  }

  private String randomClabe() {
    long account = ThreadLocalRandom.current().nextLong(0, 100_000_000_000L);
    return Clabe.fromBaseDigits("012180" + "%011d".formatted(account)).value();
  }
}
