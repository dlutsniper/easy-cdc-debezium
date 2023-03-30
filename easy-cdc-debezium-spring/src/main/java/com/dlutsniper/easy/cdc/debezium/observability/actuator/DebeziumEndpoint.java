package com.dlutsniper.easy.cdc.debezium.observability.actuator;

import io.debezium.engine.DebeziumEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

@WebEndpoint(id = "debezium")
public class DebeziumEndpoint implements DebeziumEngine.ConnectorCallback, DebeziumEngine.CompletionCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumEndpoint.class);
    private volatile boolean live = false;

    @ReadOperation
    public String debezium() {
        LOGGER.trace("Healthcheck called - live = '{}'", live);
        if (!live) {
            return String.format("{\"status\":\"%s\"}", "DOWN");
        }
        return String.format("{\"status\":\"%s\"}", "UP");
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
