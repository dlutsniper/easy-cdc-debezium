package com.dlutsniper.easy.cdc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@ConfigurationProperties(prefix = "debezium")
@Component
public class DebeziumProperties extends Properties implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebeziumProperties.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("afterPropertiesSet TODO Check");
        // TODO Check
    }
}
