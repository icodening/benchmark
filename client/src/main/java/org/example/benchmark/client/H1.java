package org.example.benchmark.client;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author icodening
 * @date 2022.12.22
 */
@State(Scope.Benchmark)
public class H1 {

    private static final String REMOTE_ADDRESS = "http://127.0.0.1:8080";

    private static final String REMOTE_ENDPOINT = REMOTE_ADDRESS + "/benchmark";

    private static final int CONCURRENT = 32;

    private final CloseableHttpClient apacheHC5;

    private final OkHttpClient okhttpClient;

    private final WebClient webClient;

    public H1() {
        this.apacheHC5 = this.buildHttpClient();
        this.okhttpClient = this.buildOkHttpClient();
        this.webClient = this.buildWebClient();
    }

    private WebClient buildWebClient() {
        return WebClient.create();
    }

    private OkHttpClient buildOkHttpClient() {
        return new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();
    }

    private CloseableHttpClient buildHttpClient() {
        return HttpClientBuilder.create()
                .build();
    }

    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void apacheHC5() throws Throwable {
        try (CloseableHttpResponse response = apacheHC5.execute(new HttpGet(REMOTE_ENDPOINT))) {
            HttpEntity entity = response.getEntity();
            try (InputStream content = entity.getContent()) {
                byte[] data = new byte[8192];
                while ((content.read(data)) != -1) {

                }
            }
        }
    }

    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void webClient() {
        this.webClient
                .get()
                .uri(REMOTE_ENDPOINT)
                .exchangeToFlux(Flux::just)
                .blockFirst();
    }


    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void okHttpClient() throws Throwable {
        Request request = new Request.Builder()
                .url(REMOTE_ENDPOINT)
                .get()
                .build();
        Response response = this.okhttpClient
                .newCall(request)
                .execute();
        ResponseBody body = response.body();
        if (body != null) {
            body.bytes();
        }
    }

    public static void main(String[] args) throws Throwable {
        Options opt;
        ChainedOptionsBuilder optBuilder = new OptionsBuilder()
                .include(H1.class.getSimpleName())
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(10))
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(10))
                .threads(CONCURRENT).forks(1);
        opt = optBuilder.build();

        new Runner(opt).run();
    }
}
