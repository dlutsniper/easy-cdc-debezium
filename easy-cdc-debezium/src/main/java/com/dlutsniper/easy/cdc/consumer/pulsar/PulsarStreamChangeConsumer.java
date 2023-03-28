package com.dlutsniper.easy.cdc.consumer.pulsar;

import com.dlutsniper.easy.cdc.base.BaseChangeConsumer;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PulsarStreamChangeConsumer extends BaseChangeConsumer implements DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PulsarStreamChangeConsumer.class);

    @Override
    public void handleBatch(List<ChangeEvent<Object, Object>> records, DebeziumEngine.RecordCommitter<ChangeEvent<Object, Object>> committer) throws InterruptedException {
        LOGGER.info(records.toString());
    }
}
