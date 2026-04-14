package com.stockpulse.controller;

import com.stockpulse.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // GET /api/portfolio/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<?> summary(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    portfolioService.getPortfolioSummary(userId)
        ));
    }
}