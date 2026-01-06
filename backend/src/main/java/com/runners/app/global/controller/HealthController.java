package com.runners.app.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Operation(summary = "Health check", description = "서비스 상태 확인용 엔드포인트")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
            Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
            )
        );
    }
}
