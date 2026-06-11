package mx.centinela.bootstrap.infrastructure.health;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

/**
 * Reports Kafka connectivity under {@code /actuator/health}. Spring Boot ships health indicators
 * for the datasource and Redis out of the box, but not for Kafka — without this, the service could
 * report UP while being unable to consume transactions.
 */
@Component("kafka")
public class KafkaHealthIndicator implements HealthIndicator {

  private static final long TIMEOUT_SECONDS = 3;

  private final KafkaAdmin kafkaAdmin;

  public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
    this.kafkaAdmin = kafkaAdmin;
  }

  @Override
  public Health health() {
    try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      DescribeClusterResult cluster = client.describeCluster();
      String clusterId = cluster.clusterId().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      int nodeCount = cluster.nodes().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).size();
      return Health.up().withDetail("clusterId", clusterId).withDetail("nodes", nodeCount).build();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return Health.down(e).build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
