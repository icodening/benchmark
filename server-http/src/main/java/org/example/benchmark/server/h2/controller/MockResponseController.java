package org.example.benchmark.server.h2.controller;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author icodening
 * @date 2022.12.22
 */
@RestController
public class MockResponseController {

    private static final DataBufferFactory FACTORY = DefaultDataBufferFactory.sharedInstance;

    private static final DataSize DEFAULT_SIZE = DataSize.ofKilobytes(1);

    private final Mono<DataBuffer> data;

    public MockResponseController() {
        DataSize responseSize = DEFAULT_SIZE;
        int size = (int) responseSize.toBytes();
        StringBuilder body = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            String randomString = UUID.randomUUID().toString();
            body.append(randomString.charAt(0));
        }
        this.data = Mono.defer(() -> Mono.just(FACTORY.wrap(body.toString().getBytes())));
    }

    @RequestMapping("/benchmark")
    public Mono<DataBuffer> benchmark() {
        return data;
    }
}
