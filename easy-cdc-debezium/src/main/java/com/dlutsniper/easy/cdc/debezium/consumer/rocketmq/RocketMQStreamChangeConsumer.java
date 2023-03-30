package com.dlutsniper.easy.cdc.debezium.consumer.rocketmq;

import com.dlutsniper.easy.cdc.debezium.base.BaseChangeConsumer;
import com.dlutsniper.easy.cdc.debezium.base.StreamNameMapper;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class RocketMQStreamChangeConsumer extends BaseChangeConsumer implements DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQStreamChangeConsumer.class);

    public RocketMQStreamChangeConsumer(Properties debeziumProperties, StreamNameMapper customStreamNameMapper) {
        super(debeziumProperties, customStreamNameMapper);
    }

    @Override
    public void handleBatch(List<ChangeEvent<Object, Object>> records, DebeziumEngine.RecordCommitter<ChangeEvent<Object, Object>> committer) throws InterruptedException {
        LOGGER.info(records.toString());
    }
}
