package com.procgrid.productservice.config;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Development configuration for Kafka
 * Provides a no-op KafkaTemplate to avoid needing a running Kafka broker
 */
@Configuration
@Profile("dev")
public class KafkaDevConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new ProducerFactory<String, Object>() {
            @Override
            public Producer<String, Object> createProducer() {
                return null; // No-op producer
            }
        };
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        // Return a no-op KafkaTemplate for dev environment
        return new KafkaTemplate<String, Object>(producerFactory) {
            @Override
            public CompletableFuture<SendResult<String, Object>> send(String topic, Object data) {
                // No-op: just return completed future
                return CompletableFuture.completedFuture(null);
            }
            
            @Override
            public CompletableFuture<SendResult<String, Object>> send(String topic, String key, Object data) {
                // No-op: just return completed future
                return CompletableFuture.completedFuture(null);
            }
            
            @Override
            public CompletableFuture<SendResult<String, Object>> send(ProducerRecord<String, Object> record) {
                // No-op: just return completed future
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}
