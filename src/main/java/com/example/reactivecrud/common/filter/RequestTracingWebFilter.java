package com.example.reactivecrud.common.filter;

import com.example.reactivecrud.common.service.RequestTraceService;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RequestTracingWebFilter implements WebFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String RESPONSE_TIME_HEADER = "X-Response-Time-Ms";

    private final RequestTraceService requestTraceService;

    public RequestTracingWebFilter(RequestTraceService requestTraceService) {
        this.requestTraceService = requestTraceService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();

        String requestId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER))
                .filter(value -> !value.isBlank())
                .orElseGet(requestTraceService::createRequestId);

        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        exchange.getResponse().beforeCommit(() -> {
            long duration = Math.max(0, System.currentTimeMillis() - start);
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set(RESPONSE_TIME_HEADER, String.valueOf(duration));
            return Mono.empty();
        });

        return chain.filter(exchange);
    }
}
