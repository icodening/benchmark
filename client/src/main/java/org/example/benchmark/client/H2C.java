package org.example.benchmark.client;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.Method;
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
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author icodening
 * @date 2022.12.22
 */
@State(Scope.Benchmark)
public class H2C {

    private static final int CONCURRENT = 32;

    private static final String REMOTE_ADDRESS = "http://127.0.0.1:8080";

    private static final String REMOTE_ENDPOINT = REMOTE_ADDRESS + "/benchmark";

    private final HttpClient nettyClient;

    private final OkHttpClient okHttpClient;

    private final CloseableHttpAsyncClient hc5;

    public H2C() {
        this.hc5 = this.buildHC5();
        this.okHttpClient = this.buildOkHttpClient();
        this.nettyClient = buildNettyClient();
    }

    private OkHttpClient buildOkHttpClient() {
        return new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE))
                .build();
    }

    private HttpClient buildNettyClient() {
        return HttpClient.create()
                .protocol(HttpProtocol.H2C);
    }

    private CloseableHttpAsyncClient buildHC5() {
        CloseableHttpAsyncClient client = HttpAsyncClients.customHttp2()
                .build();
        client.start();
        return client;
    }

    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void apacheHC5() throws Throwable {
        SimpleHttpRequest request = SimpleHttpRequest.create(Method.GET, URI.create(REMOTE_ENDPOINT));
        Future<SimpleHttpResponse> future = hc5.execute(request, null);
        future.get();
    }

    @BenchmarkMode({Mode.Throughput})
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void reactorNetty() {
        this.nettyClient.get()
                .uri(REMOTE_ENDPOINT)
                .responseContent()
                .asByteArray()
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
        Response response = this.okHttpClient
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
                .include(H2C.class.getSimpleName())
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(10))
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(10))
                .threads(CONCURRENT).forks(1);
        opt = optBuilder.build();

        new Runner(opt).run();

    }
}
