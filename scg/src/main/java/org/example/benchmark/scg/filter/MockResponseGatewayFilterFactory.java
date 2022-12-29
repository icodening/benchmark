package org.example.benchmark.scg.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author icodening
 * @date 2022.12.22
 */
public class MockResponseGatewayFilterFactory extends AbstractGatewayFilterFactory<MockResponseGatewayFilterFactory.Config> {
    private static final String MOCK_FLAG = "mock from gateway server:";

    public MockResponseGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("responseSize");
    }

    @Override
    public GatewayFilter apply(Config config) {
        long size = config.getResponseSize().toBytes();
        StringBuilder body = new StringBuilder((int) size);
        body.append(MOCK_FLAG);
        for (int i = 0; i < size - MOCK_FLAG.length(); i++) {
            String randomString = UUID.randomUUID().toString();
            body.append(randomString.charAt(0));
        }
        byte[] data = body.toString().getBytes();
        return (exchange, chain) -> {
            ServerWebExchangeUtils.setAlreadyRouted(exchange);
            ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatus.OK);
            ServerHttpResponse response = exchange.getResponse();
            DataBufferFactory factory = response.bufferFactory();
            response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            response.getHeaders().setContentLength(size);
            return response.writeWith(Mono.just(factory.wrap(data)));
        };
    }

    public static class Config {

        private DataSize responseSize;

        public DataSize getResponseSize() {
            return responseSize;
        }

        public void setResponseSize(DataSize responseSize) {
            this.responseSize = responseSize;
        }
    }
}
