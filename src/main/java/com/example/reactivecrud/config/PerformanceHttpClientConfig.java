package com.example.reactivecrud.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class PerformanceHttpClientConfig {

    @Bean
    ConnectionProvider performanceConnectionProvider(
            @Value("${app.performance.max-connections:100}") int maxConnections,
            @Value("${app.performance.pending-acquire-max-count:500}") int pendingAcquireMaxCount,
            @Value("${app.performance.max-idle-seconds:30}") long maxIdleSeconds
    ) {
        return ConnectionProvider.builder("performance-pool")
                .maxConnections(maxConnections)
                .pendingAcquireMaxCount(pendingAcquireMaxCount)
                .maxIdleTime(Duration.ofSeconds(maxIdleSeconds))
                .build();
    }

    @Bean
    HttpClient performanceHttpClient(ConnectionProvider performanceConnectionProvider) {
        return HttpClient.create(performanceConnectionProvider)
                .compress(true)
                .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5));
    }

    @Bean
    WebClient.Builder webClientBuilder(HttpClient performanceHttpClient) {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(performanceHttpClient));
    }
}
