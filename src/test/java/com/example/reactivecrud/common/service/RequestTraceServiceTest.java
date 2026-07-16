package com.example.reactivecrud.common.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RequestTraceServiceTest {

    @Test
    void createRequestIdShouldGenerateNonBlankValue() {
        RequestTraceService service = new RequestTraceService();

        String requestId = service.createRequestId();

        Assertions.assertNotNull(requestId);
        Assertions.assertFalse(requestId.isBlank());
    }
}
