package org.example.benchmark.client;

import feign.Feign;
import feign.jaxrs2.JAXRS2Contract;
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
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private final RestTemplate restTemplate;

    private final FeignService feignService;

    public H1() {
        this.apacheHC5 = this.buildHttpClient();
        this.okhttpClient = this.buildOkHttpClient();
        this.webClient = this.buildWebClient();
        this.restTemplate = this.buildRestTemplate();
        this.feignService = this.buildFeignService();
    }

    private FeignService buildFeignService() {
        return Feign.builder()
                .client(new feign.okhttp.OkHttpClient(okhttpClient))
                .contract(new JAXRS2Contract())
                .target(FeignService.class, REMOTE_ADDRESS);
    }

    private RestTemplate buildRestTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(okhttpClient));
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

    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void apacheHC5Benchmark() throws Throwable {
        try (CloseableHttpResponse response = apacheHC5.execute(new HttpGet(REMOTE_ENDPOINT))) {
            HttpEntity entity = response.getEntity();
            try (InputStream content = entity.getContent()) {
                byte[] data = new byte[1024];
                ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
                int len;
                while ((len = content.read(data)) != -1) {
                    bos.write(data, 0, len);
                }
                String resp = bos.toString();
            }
        }
    }

    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void restTemplateBenchmark() {
        String resp = this.restTemplate.getForObject(URI.create(REMOTE_ENDPOINT), String.class);
    }

    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void feignClientBenchmark() {
        String resp = feignService.benchmark();
    }

    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void webClientBenchmark() {
        this.webClient
                .get()
                .uri(REMOTE_ENDPOINT)
                .exchangeToFlux(Flux::just)
                .blockFirst();
    }


    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void okHttpClientBenchmark() throws Throwable {
        Request request = new Request.Builder()
                .url(REMOTE_ENDPOINT)
                .get()
                .build();
        Response response = this.okhttpClient
                .newCall(request)
                .execute();
        ResponseBody body = response.body();
        if (body != null) {
            new String(body.bytes());
        }
    }

    public static void main(String[] args) throws Throwable {
        Map<String, String> properties = parseArguments(args);
        //-wi 1 -i 3 -f 1 -rf json
        int warmupIterations = getInt(properties, "-w", 3);
        int measurementIterations = getInt(properties, "-i", 3);
        int forks = getInt(properties, "-f", 1);
        int threads = getInt(properties, "-t", CONCURRENT);
        String fileName = getString(properties, "-rf", "jmh_result.json");
        String suffix = "json";
        if (fileName.contains(".")) {
            suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        ResultFormatType resultFormatType = ResultFormatType.valueOf(suffix.toUpperCase());

        Options opt = new OptionsBuilder()
                .detectJvmArgs()
                .resultFormat(resultFormatType)
                .result(fileName)
                .include(H1.class.getSimpleName())
                .warmupIterations(warmupIterations)
                .warmupTime(TimeValue.seconds(10))
                .measurementIterations(measurementIterations)
                .measurementTime(TimeValue.seconds(10))
                .threads(threads)
                .forks(forks)
                .build();
        new Runner(opt).run();
    }

    private static int getInt(Map<String, String> properties, String name, int defaultValue) {
        String value = properties.get(name);
        int result = defaultValue;
        try {
            if (value != null) {
                result = Integer.parseInt(value);
            }
        } catch (NumberFormatException ignore) {
        }
        return result;
    }

    private static String getString(Map<String, String> properties, String name, String defaultValue) {
        return properties.getOrDefault(name, defaultValue);
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> properties = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            String argName = args[i];
            String argValue = null;
            if (i + 1 < args.length) {
                argValue = args[i + 1];
            }
            properties.put(argName, argValue);
        }
        return properties;
    }
}
