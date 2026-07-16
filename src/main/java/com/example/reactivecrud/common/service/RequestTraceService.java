package com.example.reactivecrud.common.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RequestTraceService {

    public String createRequestId() {
        return UUID.randomUUID().toString();
    }
}
