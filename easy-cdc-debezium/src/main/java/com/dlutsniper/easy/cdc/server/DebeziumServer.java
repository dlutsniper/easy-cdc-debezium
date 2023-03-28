package com.dlutsniper.easy.cdc.server;

import com.dlutsniper.easy.cdc.observability.actuator.DebeziumEndpoint;
import com.dlutsniper.easy.cdc.observability.actuator.DebeziumHealthIndicator;
import com.dlutsniper.easy.cdc.config.DebeziumProperties;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Avro;
import io.debezium.engine.format.CloudEvents;
import io.debezium.engine.format.Json;
import io.debezium.engine.format.Protobuf;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <pre>
 * Original Source Code:
 * https://github.com/debezium/debezium-server/blob/main/debezium-server-core/src/main/java/io/debezium/server/DebeziumServer.java
 * </pre>
 */
public class DebeziumServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumServer.class);
    private static final Pattern SHELL_PROPERTY_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+_+[a-zA-Z0-9_]+$");
    @Autowired
    private DebeziumProperties debeziumProperties;
    @Autowired
    private DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> changeConsumer;
    @Autowired(required = false)
    private DebeziumEndpoint debeziumEndpoint;
    @Autowired(required = false)
    private DebeziumHealthIndicator debeziumHealthIndicator;
    private DebeziumEngine<ChangeEvent<Object, Object>> engine = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        String name = (String) debeziumProperties.get("sink.type");
        LOGGER.info("Debezium config property sink.type is {}", name);
        //
        LOGGER.info("Consumer '{}' instantiated", changeConsumer.getClass().getName());
        //
        Properties props = new Properties();
        propertiesToProperties(debeziumProperties, props, "source.", "", true);
        props.setProperty("name", name);
        LOGGER.debug("Configuration for DebeziumEngine: {}", props);
        //
        Object health = null;
        if (debeziumEndpoint != null) {
            health = debeziumEndpoint;
        }
        if (debeziumHealthIndicator != null) {
            health = debeziumHealthIndicator;
        }
        //
        final Class keyFormat = getFormat(props, "format.key");
        final Class valueFormat = getFormat(props, "format.value");
        engine = DebeziumEngine.create(keyFormat, valueFormat) //
                .using(props) //
                .using((DebeziumEngine.ConnectorCallback) health) //
                .using((DebeziumEngine.CompletionCallback) health) //
                .notifying(changeConsumer) //
                .build();
        //
        executor.execute(() -> {
            try {
                engine.run();
            } finally {
                LOGGER.info("engine run finally");
            }
        });
        LOGGER.info("Engine executor started");
    }

    private void propertiesToProperties(Properties properties, Properties props, String oldPrefix, String newPrefix, boolean overwrite) {
        Iterator<Object> it = properties.keys().asIterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            String updatedPropertyName = null;
            if (SHELL_PROPERTY_NAME_PATTERN.matcher(name).matches()) {
                updatedPropertyName = name.replace("_", ".").toLowerCase();
            }
            if (updatedPropertyName != null && updatedPropertyName.startsWith(oldPrefix)) {
                String finalPropertyName = newPrefix + updatedPropertyName.substring(oldPrefix.length());
                if (overwrite || !props.containsKey(finalPropertyName)) {
                    props.setProperty(finalPropertyName, (String) properties.get(name));
                }
            } else if (name.startsWith(oldPrefix)) {
                String finalPropertyName = newPrefix + name.substring(oldPrefix.length());
                if (overwrite || !props.containsKey(finalPropertyName)) {
                    props.setProperty(finalPropertyName, (String) properties.get(name));
                }
            }
        }
    }

    private Class<?> getFormat(Properties properties, String key) {
        final String formatName = (String) properties.get(key);
        //final String formatName = config.getOptionalValue(key, String.class).orElse(Json.class.getSimpleName().toLowerCase());
        if (Json.class.getSimpleName().toLowerCase().equals(formatName)) {
            return Json.class;
        } else if (CloudEvents.class.getSimpleName().toLowerCase().equals(formatName)) {
            return CloudEvents.class;
        } else if (Avro.class.getSimpleName().toLowerCase().equals(formatName)) {
            return Avro.class;
        } else if (Protobuf.class.getSimpleName().toLowerCase().equals(formatName)) {
            return Protobuf.class;
        }
        //throw new DebeziumException("Unknown format '" + formatName + "' for option " + "'" + key + "'");
        return Json.class;
    }

    @PreDestroy
    public void destroy() {
        LOGGER.info("Received request to stop the engine");
        try {
            engine.close();
            executor.shutdown();
            while (!executor.awaitTermination(5/*termination.wait*/, TimeUnit.SECONDS)) {
                LOGGER.info("Waiting another 5 seconds for the embedded engine to shut down");
            }
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
