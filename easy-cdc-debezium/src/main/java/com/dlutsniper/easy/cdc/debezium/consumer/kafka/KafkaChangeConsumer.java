package com.dlutsniper.easy.cdc.debezium.consumer.kafka;

import com.dlutsniper.easy.cdc.debezium.base.BaseChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.base.StreamNameMapper;
import io.debezium.DebeziumException;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * <pre>
 * Original Source Code:
 * https://github.com/debezium/debezium-server/blob/main/debezium-server-kafka/src/main/java/io/debezium/server/kafka/KafkaChangeConsumer.java
 *
 * 1.- @Named("kafka") @Dependent
 * 2.String PROP_PREFIX
 * 3.DebeziumProperties debeziumProperties
 * </pre>
 */
public class KafkaChangeConsumer extends BaseChangeConsumer implements DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaChangeConsumer.class);
    private static final String PROP_PREFIX = "sink.kafka."; // debezium.sink.kafka.
    private static final String PROP_PREFIX_PRODUCER = PROP_PREFIX + "producer.";
    private KafkaProducer<Object, Object> producer;

    public KafkaChangeConsumer(Properties debeziumProperties, StreamNameMapper customStreamNameMapper) {
        super(debeziumProperties, customStreamNameMapper);
    }

    public void start() {
        Map<String, Object> map = getConfigSubset(debeziumProperties, PROP_PREFIX_PRODUCER);
        producer = new KafkaProducer<>(map);
        LOGGER.info("consumer started...");
    }

    public void stop() {
        LOGGER.info("consumer destroyed...");
        if (producer != null) {
            try {
                producer.close(Duration.ofSeconds(5));
            } catch (Throwable t) {
                LOGGER.warn("Could not close producer {}", t);
            }
        }
    }

    @Override
    public void handleBatch(final List<ChangeEvent<Object, Object>> records, final DebeziumEngine.RecordCommitter<ChangeEvent<Object, Object>> committer) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(records.size());
        for (ChangeEvent<Object, Object> record : records) {
            try {
                LOGGER.trace("Received event '{}'", record);
                producer.send(new ProducerRecord<>(record.destination(), record.key(), record.value()), (metadata, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Failed to send record to {}:", record.destination(), exception);
                        throw new DebeziumException(exception);
                    } else {
                        LOGGER.trace("Sent message with offset: {}", metadata.offset());
                        latch.countDown();
                    }
                });
                committer.markProcessed(record);
            } catch (Exception e) {
                throw new DebeziumException(e);
            }
        }
        latch.await();
        committer.markBatchFinished();
    }
}
