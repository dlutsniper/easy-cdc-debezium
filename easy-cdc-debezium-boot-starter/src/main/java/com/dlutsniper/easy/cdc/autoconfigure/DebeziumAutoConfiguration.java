package com.dlutsniper.easy.cdc.autoconfigure;

import com.dlutsniper.easy.cdc.config.DebeziumProperties;
import com.dlutsniper.easy.cdc.consumer.kafka.KafkaChangeConsumer;
import com.dlutsniper.easy.cdc.consumer.noop.NoOpStreamChangeConsumer;
import com.dlutsniper.easy.cdc.consumer.pulsar.PulsarStreamChangeConsumer;
import com.dlutsniper.easy.cdc.consumer.redis.RedisStreamChangeConsumer;
import com.dlutsniper.easy.cdc.consumer.rocketmq.RocketMQStreamChangeConsumer;
import com.dlutsniper.easy.cdc.observability.actuator.DebeziumEndpoint;
import com.dlutsniper.easy.cdc.observability.actuator.DebeziumHealthIndicator;
import com.dlutsniper.easy.cdc.observability.metrics.DebeziumMetrics;
import com.dlutsniper.easy.cdc.server.DebeziumServer;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(DebeziumEngine.class)
@EnableConfigurationProperties(DebeziumProperties.class)
@ConditionalOnProperty(prefix = "easy-cdc", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class DebeziumAutoConfiguration {
    @ConditionalOnClass(org.apache.kafka.clients.producer.KafkaProducer.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "kafka")
    @ConditionalOnMissingBean
    @Bean
    public KafkaChangeConsumer kafkaChangeConsumer() {
        return new KafkaChangeConsumer();
    }

    @ConditionalOnClass(io.debezium.storage.redis.RedisClient.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "redis")
    @ConditionalOnMissingBean
    @Bean
    public RedisStreamChangeConsumer redisStreamChangeConsumer() {
        return new RedisStreamChangeConsumer();
    }

    //@ConditionalOnClass(io.debezium.storage.redis.RedisClient.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "roketmq")
    @ConditionalOnMissingBean
    @Bean
    public RocketMQStreamChangeConsumer rocketmqStreamChangeConsumer() {
        return new RocketMQStreamChangeConsumer();
    }

    //@ConditionalOnClass(io.debezium.storage.redis.RedisClient.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "pulsar")
    @ConditionalOnMissingBean
    @Bean
    public PulsarStreamChangeConsumer pulsarStreamChangeConsumer() {
        return new PulsarStreamChangeConsumer();
    }

    @ConditionalOnMissingBean
    @Bean
    public DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> changeConsumer() {
        return new NoOpStreamChangeConsumer();
    }

    @Bean
    public DebeziumServer debeziumServer() {
        return new DebeziumServer();
    }

    @ConditionalOnClass(org.springframework.boot.actuate.health.HealthIndicator.class)
    @ConditionalOnProperty(prefix = "dlutsniper", name = "health", havingValue = "debezium")
    @ConditionalOnMissingBean
    @Bean
    public DebeziumEndpoint debeziumEndpoint() {
        return new DebeziumEndpoint();
    }

    @ConditionalOnClass(org.springframework.boot.actuate.health.HealthIndicator.class)
    @ConditionalOnProperty(prefix = "dlutsniper", name = "health", havingValue = "health")
    @ConditionalOnMissingBean
    @Bean
    public DebeziumHealthIndicator debeziumHealthIndicator() {
        return new DebeziumHealthIndicator();
    }

    @Bean
    public DebeziumMetrics debeziumMetrics() {
        return new DebeziumMetrics();
    }
    // TODO OpenTelemetry
}
