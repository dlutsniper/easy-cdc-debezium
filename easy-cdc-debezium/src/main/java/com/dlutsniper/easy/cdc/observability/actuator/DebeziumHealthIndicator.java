package com.dlutsniper.easy.cdc.observability.actuator;

import io.debezium.engine.DebeziumEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class DebeziumHealthIndicator implements HealthIndicator, DebeziumEngine.ConnectorCallback, DebeziumEngine.CompletionCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumHealthIndicator.class);
    private volatile boolean live = false;

    @Override
    public Health health() {
        LOGGER.trace("Healthcheck called - live = '{}'", live);
        if (!live) {
            return Health.down().build();
        }
        return Health.up().build();
    }

    @Override
    public void connectorStarted() {
    }

    @Override
    public void connectorStopped() {
    }

    @Override
    public void taskStarted() {
        live = true;
    }

    @Override
    public void taskStopped() {
    }

    @Override
    public void handle(boolean success, String message, Throwable error) {
        String logMessage = String.format("Connector completed: success = '%s', message = '%s', error = '%s'", success, message, error);
        if (success) {
            LOGGER.info(logMessage);
            live = true;
        } else {
            LOGGER.error(logMessage, error);
            live = false;
        }
    }
}
