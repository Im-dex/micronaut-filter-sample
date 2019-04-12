package filters;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

@Controller("/test")
class TestController {
    @Get("/ping")
    String ping(@Header("X-HEADER") String header) {
        return "pong: " + header;
    }

    @Get("/hello/{name}")
    String hello(String name) {
        return "Hello, " + name;
    }
}

@Client(id = "basic", path = "/test")
interface BasicClient {
    @Get("/hello/{name}")
    String hello(String name);
}

@Client(id = "filtered", path = "/test")
interface FilteredClient {
    @Get("/ping")
    String ping();
}

@Filter(serviceId = "filtered")
class ClientFilter implements HttpClientFilter {
    @Inject
    BasicClient basicClient;

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        String value = basicClient.hello("filter");
        return chain.proceed(request.header("X_HEADER", value));
    }
}

@MicronautTest
class FilterTest {
    @Inject
    FilteredClient filteredClient;

    @Test
    void test() {
        assert filteredClient.ping().equals("pong: Hello, filter");
    }
}
