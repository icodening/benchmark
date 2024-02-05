package org.example.benchmark.server.http.controller;

import org.example.benchmark.server.http.config.HttpServerProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author icodening
 * @date 2022.12.22
 */
@RestController
public class MockResponseController {

    private static final DataBufferFactory FACTORY = DefaultDataBufferFactory.sharedInstance;

    private final HttpServerProperties httpServerProperties;

    private final Mono<DataBuffer> data;

    private static final AtomicInteger COUNTER = new AtomicInteger();

    public MockResponseController(HttpServerProperties httpServerProperties) {
        this.httpServerProperties = httpServerProperties;
        this.data = mockData();
    }

    @RequestMapping(value = "/benchmark", produces = "text/plain")
    public Mono<DataBuffer> benchmark() {
        COUNTER.incrementAndGet();
        return data;
    }

    @RequestMapping("/count")
    public int get() {
        return COUNTER.getAndSet(0);
    }

    private Mono<DataBuffer> mockData() {
        DataSize responseSize = httpServerProperties.getMockResponseSize();
        int size = (int) responseSize.toBytes();
        StringBuilder body = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            String randomString = UUID.randomUUID().toString();
            body.append(randomString.charAt(0));
        }
        return Mono.defer(() -> Mono.just(FACTORY.wrap(body.toString().getBytes())));
    }
}
