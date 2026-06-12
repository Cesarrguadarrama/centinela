package mx.centinela.generator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import mx.centinela.generator.event.TransactionEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

  /**
   * Uses Spring Boot's ObjectMapper (JavaTimeModule registered, ISO-8601 dates) instead of the
   * default one JsonSerializer would build, and skips type headers so the event schema — not a Java
   * class name — is the contract between generator and detection engine.
   */
  @Bean
  public ProducerFactory<String, TransactionEvent> producerFactory(
      KafkaProperties kafkaProperties, ObjectMapper objectMapper) {
    Map<String, Object> config = kafkaProperties.buildProducerProperties(null);
    config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    return new DefaultKafkaProducerFactory<>(
        config, new StringSerializer(), new JsonSerializer<>(objectMapper));
  }

  @Bean
  public KafkaTemplate<String, TransactionEvent> kafkaTemplate(
      ProducerFactory<String, TransactionEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public NewTopic transactionsTopic(@Value("${centinela.kafka.topics.transactions}") String topic) {
    return TopicBuilder.name(topic).partitions(3).replicas(1).build();
  }
}
