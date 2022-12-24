package org.example.benchmark.server.h2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author icodening
 * @date 2022.12.21
 */
@SpringBootApplication
public class Http2ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Http2ServerApplication.class);
    }
}
