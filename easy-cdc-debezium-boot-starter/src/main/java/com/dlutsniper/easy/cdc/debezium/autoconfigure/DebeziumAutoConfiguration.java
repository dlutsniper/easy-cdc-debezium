package com.dlutsniper.easy.cdc.debezium.autoconfigure;

import com.dlutsniper.easy.cdc.debezium.config.DebeziumProperties;
import com.dlutsniper.easy.cdc.debezium.consumer.kafka.KafkaChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.consumer.noop.NoOpStreamChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.consumer.pulsar.PulsarStreamChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.consumer.redis.RedisStreamChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.consumer.rocketmq.RocketMQStreamChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.observability.actuator.DebeziumEndpoint;
import com.dlutsniper.easy.cdc.debezium.observability.actuator.DebeziumHealthIndicator;
import com.dlutsniper.easy.cdc.debezium.observability.metrics.DebeziumMetrics;
import com.dlutsniper.easy.cdc.debezium.server.DebeziumServer;
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
        return new KafkaChangeConsumer(null, null); // TODO
    }

    @ConditionalOnClass(io.debezium.storage.redis.RedisClient.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "redis")
    @ConditionalOnMissingBean
    @Bean
    public RedisStreamChangeConsumer redisStreamChangeConsumer() {
        return new RedisStreamChangeConsumer(null, null);
    }

    //@ConditionalOnClass(io.debezium.storage.redis.RedisClient.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "roketmq")
    @ConditionalOnMissingBean
    @Bean
    public RocketMQStreamChangeConsumer rocketmqStreamChangeConsumer() {
        return new RocketMQStreamChangeConsumer(null, null);
    }

    //@ConditionalOnClass(io.debezium.storage.redis.RedisClient.class)
    @ConditionalOnProperty(prefix = "debezium", name = "sink.type", havingValue = "pulsar")
    @ConditionalOnMissingBean
    @Bean
    public PulsarStreamChangeConsumer pulsarStreamChangeConsumer() {
        return new PulsarStreamChangeConsumer(null, null);
    }

    @ConditionalOnMissingBean
    @Bean
    public DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> changeConsumer() {
        return new NoOpStreamChangeConsumer(null, null);
    }

    @Bean
    public DebeziumServer debeziumServer() {
        return new DebeziumServer(null,null,null,null);
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
