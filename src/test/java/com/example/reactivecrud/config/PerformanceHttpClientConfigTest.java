package com.example.reactivecrud.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

class PerformanceHttpClientConfigTest {

    private final PerformanceHttpClientConfig config = new PerformanceHttpClientConfig();

    @Test
    void connectionProviderShouldBeBuiltFromPoolProperties() {
        ConnectionProvider provider = config.performanceConnectionProvider(50, 200, 15);

        Assertions.assertNotNull(provider);
        Assertions.assertEquals(50, provider.maxConnections());
    }

    @Test
    void httpClientAndWebClientBuilderShouldBeWiredFromTheProvider() {
        ConnectionProvider provider = config.performanceConnectionProvider(50, 200, 15);

        HttpClient httpClient = config.performanceHttpClient(provider);
        Assertions.assertNotNull(httpClient);

        WebClient.Builder builder = config.webClientBuilder(httpClient);
        Assertions.assertNotNull(builder);
        Assertions.assertNotNull(builder.build());
    }
}
