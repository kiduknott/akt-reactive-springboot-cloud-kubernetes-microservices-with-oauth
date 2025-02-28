package com.akt.springcloud.configserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

//@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ConfigServerApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ConfigServerApplication.class, args);
    }
}
