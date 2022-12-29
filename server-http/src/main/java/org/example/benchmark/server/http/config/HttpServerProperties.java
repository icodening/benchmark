package org.example.benchmark.server.http.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * @author icodening
 * @date 2022.12.29
 */
@ConfigurationProperties(prefix = "server")
public class HttpServerProperties {

    /**
     * mock response body size
     */
    private DataSize mockResponseSize;

    public DataSize getMockResponseSize() {
        return mockResponseSize;
    }

    public void setMockResponseSize(DataSize mockResponseSize) {
        this.mockResponseSize = mockResponseSize;
    }
}
