package com.example.reactivecrud.common.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/filter")
public class FilterDemoController {

    @GetMapping("/ping")
    public Mono<Map<String, String>> ping() {
        return Mono.just(Map.of("status", "ok"));
    }
}
