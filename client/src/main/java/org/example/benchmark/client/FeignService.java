package org.example.benchmark.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author icodening
 * @date 2022.12.22
 */
public interface FeignService {

    @GET
    @Path("/benchmark")
    String benchmark();
}
