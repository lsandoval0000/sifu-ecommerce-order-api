package org.sifu.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.sifu.avro.OrderEvent;

import java.util.Properties;

/**
 * Configuration class for Kafka producer.
 * Produces a configured Kafka producer bean for publishing OrderEvent messages.
 */
@Singleton
public class KafkaProducerConfiguration {
    @ConfigProperty(name="kafka.bootstrap.servers")
    private String bootstrapServers;
    
    @ConfigProperty(name="kafka.schema-registry.url")
    private String schemaRegistryUrl;
    
    /**
     * Creates and configures a Kafka producer for OrderEvent messages.
     * @return a configured Kafka producer instance
     */
    @Produces
    @Named("producer")
    public Producer<String, OrderEvent> createProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        properties.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        properties.put(AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION, true);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        return new KafkaProducer<>(properties);
    }
}
