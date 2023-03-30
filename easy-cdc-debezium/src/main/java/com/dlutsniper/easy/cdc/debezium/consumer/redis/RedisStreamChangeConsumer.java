package com.dlutsniper.easy.cdc.debezium.consumer.redis;

import com.dlutsniper.easy.cdc.debezium.base.BaseChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.base.StreamNameMapper;
import io.debezium.DebeziumException;
import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.DebeziumEngine.RecordCommitter;
import io.debezium.storage.redis.RedisClient;
import io.debezium.storage.redis.RedisClientConnectionException;
import io.debezium.storage.redis.RedisConnection;
import io.debezium.util.DelayStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <pre>
 * Original Source Code:
 * https://github.com/debezium/debezium-server/blob/main/debezium-server-redis/src/main/java/io/debezium/server/redis/RedisStreamChangeConsumer.java
 *
 * 1.- @Named("redis") @Dependent
 * 2.jakarta
 *   PostConstruct
 *   PreDestroy
 * 3.DebeziumProperties debeziumProperties
 * </pre>
 */
public class RedisStreamChangeConsumer extends BaseChangeConsumer implements DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisStreamChangeConsumer.class);
    private static final String DEBEZIUM_REDIS_SINK_CLIENT_NAME = "debezium:redis:sink";
    private static final String EXTENDED_MESSAGE_KEY_KEY = "key";
    private static final String EXTENDED_MESSAGE_VALUE_KEY = "value";
    private RedisClient client;
    private Function<ChangeEvent<Object, Object>, Map<String, String>> recordMapFunction;
    private RedisMemoryThreshold isMemoryOk;
    private RedisStreamChangeConsumerConfig config;

    public RedisStreamChangeConsumer(Properties debeziumProperties, StreamNameMapper customStreamNameMapper) {
        super(debeziumProperties, customStreamNameMapper);
    }

    void connect() {
        Map<String, Object> map = new HashMap<>((Map) debeziumProperties);
        Configuration configuration = Configuration.from(map);
        config = new RedisStreamChangeConsumerConfig(configuration);
        String messageFormat = config.getMessageFormat();
        if (RedisStreamChangeConsumerConfig.MESSAGE_FORMAT_EXTENDED.equals(messageFormat)) {
            recordMapFunction = record -> {
                Map<String, String> recordMap = new LinkedHashMap<>();
                String key = (record.key() != null) ? getString(record.key()) : config.getNullKey();
                String value = (record.value() != null) ? getString(record.value()) : config.getNullValue();
//                Map<String, String> headers = convertHeaders(record);
                recordMap.put(EXTENDED_MESSAGE_KEY_KEY, key);
                recordMap.put(EXTENDED_MESSAGE_VALUE_KEY, value);
//                for (Map.Entry<String, String> entry : headers.entrySet()) {
//                    recordMap.put(entry.getKey().toUpperCase(Locale.ROOT), entry.getValue());
//                }
                return recordMap;
            };
        } else if (RedisStreamChangeConsumerConfig.MESSAGE_FORMAT_COMPACT.equals(messageFormat)) { // 默认
            recordMapFunction = record -> {
                String key = (record.key() != null) ? getString(record.key()) : config.getNullKey();
                String value = (record.value() != null) ? getString(record.value()) : config.getNullValue();
                return Map.of(key, value);
            };
        }
        RedisConnection redisConnection = new RedisConnection(config.getAddress(), config.getUser(), config.getPassword(), config.getConnectionTimeout(), config.getSocketTimeout(), config.isSslEnabled());
        client = redisConnection.getRedisClient(DEBEZIUM_REDIS_SINK_CLIENT_NAME, config.isWaitEnabled(), config.getWaitTimeout(), config.isWaitRetryEnabled(), config.getWaitRetryDelay());
        isMemoryOk = new RedisMemoryThreshold(client, config);
    }

    //@PreDestroy
    void close() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Exception while closing Jedis: {}", client, e);
        } finally {
            client = null;
        }
    }

    /**
     * Split collection to batches by batch size using a stream
     */
    private <T> Stream<List<T>> batches(List<T> source, int length) {
        if (source.isEmpty()) {
            return Stream.empty();
        }
        int size = source.size();
        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }

    @Override
    public void handleBatch(List<ChangeEvent<Object, Object>> records, RecordCommitter<ChangeEvent<Object, Object>> committer) throws InterruptedException {
        DelayStrategy delayStrategy = DelayStrategy.exponential(Duration.ofMillis(config.getInitialRetryDelay()), // 300
                Duration.ofMillis(config.getMaxRetryDelay())); // 10000
        LOGGER.trace("Handling a batch of {} records", records.size());
        batches(records, config.getBatchSize()).forEach(batch -> {
            boolean completedSuccessfully = false;
            // Clone the batch and remove the records that have been successfully processed.
            // Move to the next batch once this list is empty.
            List<ChangeEvent<Object, Object>> clonedBatch = batch.stream().collect(Collectors.toList());
            // As long as we failed to execute the current batch to the stream, we should retry if the reason was either a connection error or OOM in Redis.
            while (!completedSuccessfully) {
                if (client == null) { // Try to reconnect
                    try {
                        connect();
                        continue; // Managed to establish a new connection to Redis, avoid a redundant retry
                    } catch (Exception e) {
                        close();
                        LOGGER.error("Can't connect to Redis", e);
                    }
                } else if (isMemoryOk.check()) {
                    try {
                        LOGGER.trace("Preparing a Redis Pipeline of {} records", clonedBatch.size());
                        List<SimpleEntry<String, Map<String, String>>> recordsMap = new ArrayList<>(clonedBatch.size());
                        for (ChangeEvent<Object, Object> record : clonedBatch) {
                            String destination = streamNameMapper.map(record.destination());
                            Map<String, String> recordMap2 = recordMapFunction.apply(record);
                            recordsMap.add(new SimpleEntry<>(destination, recordMap2));
                        }
                        List<String> responses = client.xadd(recordsMap);
                        List<ChangeEvent<Object, Object>> processedRecords = new ArrayList<ChangeEvent<Object, Object>>();
                        int index = 0;
                        int totalOOMResponses = 0;
                        for (String message : responses) {
                            // When Redis reaches its max memory limitation, an OOM error message will be retrieved.
                            // In this case, we will retry execute the failed commands, assuming some memory will be freed eventually as result
                            // of evicting elements from the stream by the target DB.
                            if (message.contains("OOM command not allowed when used memory > 'maxmemory'")) {
                                totalOOMResponses++;
                            } else {
                                // Mark the record as processed
                                ChangeEvent<Object, Object> currentRecord = clonedBatch.get(index);
                                committer.markProcessed(currentRecord);
                                processedRecords.add(currentRecord);
                            }
                            index++;
                        }
                        clonedBatch.removeAll(processedRecords);
                        if (totalOOMResponses > 0) {
                            LOGGER.warn("Redis runs OOM, {} command(s) failed", totalOOMResponses);
                        }
                        if (clonedBatch.size() == 0) {
                            completedSuccessfully = true;
                        }
                    } catch (RedisClientConnectionException jce) {
                        LOGGER.error("Connection error", jce);
                        close();
                    } catch (Exception e) {
                        LOGGER.error("Unexpected Exception", e);
                        throw new DebeziumException(e);
                    }
                } else {
                    LOGGER.warn("Stopped consuming records!");
                } // 1.connect ng 2.process 3.redis mem ng
                // Failed to execute the transaction, retry...
                delayStrategy.sleepWhen(!completedSuccessfully);
            } // while
        }); // forEach
        // Mark the whole batch as finished once the sub batches completed
        committer.markBatchFinished();
    }
}
