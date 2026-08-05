package com.example.reactivecrud.common.filter;

import com.example.reactivecrud.common.service.RequestTraceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.test.StepVerifier;

class RequestTracingWebFilterTest {

    private final RequestTracingWebFilter filter = new RequestTracingWebFilter(new RequestTraceService());

    // A chain that completes the response so beforeCommit callbacks (timing header) run.
    private final WebFilterChain completingChain = exchange -> exchange.getResponse().setComplete();

    @Test
    void shouldReuseIncomingRequestId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/filter/ping")
                        .header(RequestTracingWebFilter.REQUEST_ID_HEADER, "trace-123"));

        StepVerifier.create(filter.filter(exchange, completingChain)).verifyComplete();

        Assertions.assertEquals("trace-123",
                exchange.getResponse().getHeaders().getFirst(RequestTracingWebFilter.REQUEST_ID_HEADER));
        assertResponseTimeIsNonNegative(exchange);
    }

    @Test
    void shouldGenerateRequestIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/filter/ping"));

        StepVerifier.create(filter.filter(exchange, completingChain)).verifyComplete();

        String requestId = exchange.getResponse().getHeaders().getFirst(RequestTracingWebFilter.REQUEST_ID_HEADER);
        Assertions.assertNotNull(requestId);
        Assertions.assertFalse(requestId.isBlank());
        assertResponseTimeIsNonNegative(exchange);
    }

    private void assertResponseTimeIsNonNegative(MockServerWebExchange exchange) {
        String responseTime = exchange.getResponse().getHeaders().getFirst(RequestTracingWebFilter.RESPONSE_TIME_HEADER);
        Assertions.assertNotNull(responseTime);
        Assertions.assertTrue(Long.parseLong(responseTime) >= 0);
    }
}
