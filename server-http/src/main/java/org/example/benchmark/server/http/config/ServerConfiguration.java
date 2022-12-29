package org.example.benchmark.server.http.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author icodening
 * @date 2022.12.29
 */
@Configuration
@EnableConfigurationProperties(HttpServerProperties.class)
public class ServerConfiguration {

}

