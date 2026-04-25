package org.sifu.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.sifu.avro.OrderEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Configuration class for Kafka consumer.
 * Produces a configured Kafka consumer bean for consuming OrderEvent messages.
 */
@Singleton
public class KafkaConsumerConfiguration {
    @ConfigProperty(name="kafka.bootstrap.servers")
    private String bootstrapServers;

    @ConfigProperty(name="kafka.schema-registry.url")
    private String schemaRegistryUrl;

    @ConfigProperty(name="kafka.consumer.order-notification.group-id")
    private String consumerGroupId;

    @ConfigProperty(name="kafka.consumer.order-notification.client-id")
    private String consumerClientId;
    
    /**
     * Creates and configures a Kafka consumer for OrderEvent messages.
     * @return a configured Kafka consumer instance
     */
    @Produces
    @Named("consumer")
    public Consumer<String, OrderEvent> consumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        properties.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        properties.put(AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION, true);
        properties.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        properties.putIfAbsent(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId + "-" + getHostname());
        properties.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        properties.putIfAbsent(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new KafkaConsumer<>(properties);
    }
    
    /**
     * Gets the local hostname for use in client ID generation.
     * @return the local hostname, or "UnknownHost" if it cannot be determined
     */
    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UnknownHost";
        }
    }
}
