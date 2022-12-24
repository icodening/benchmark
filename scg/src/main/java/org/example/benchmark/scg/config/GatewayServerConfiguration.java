package org.example.benchmark.scg.config;

import org.example.benchmark.scg.filter.MockResponseGatewayFilterFactory;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

/**
 * @author icodening
 * @date 2022.12.22
 */
@Configuration
public class GatewayServerConfiguration {

    @Bean
    @ConditionalOnEnabledFilter
    public MockResponseGatewayFilterFactory mockResponseFilter() {
        return new MockResponseGatewayFilterFactory();
    }

    @Bean
    public HttpClientCustomizer h2cClientCustomizer() {
        return new HttpClientCustomizer() {
            @Override
            public HttpClient customize(HttpClient httpClient) {
                return httpClient.protocol(HttpProtocol.HTTP11, HttpProtocol.H2C);
            }
        };
    }
}
