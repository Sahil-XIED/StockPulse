package com.stockpulse.controller;

import com.stockpulse.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // GET /api/stocks
    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    stockService.getAllStocks()
        ));
    }

    // GET /api/stocks/{symbol}
    @GetMapping("/{symbol}")
    public ResponseEntity<?> getOne(@PathVariable String symbol) {
        return stockService.getBySymbol(symbol)
                .map(s -> ResponseEntity.ok(Map.of("success", true, "data", s)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/stocks/search?q=tata
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    stockService.search(q)
        ));
    }

    // GET /api/stocks/{symbol}/history
    @GetMapping("/{symbol}/history")
    public ResponseEntity<?> history(@PathVariable String symbol) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    stockService.getHistory(symbol)
        ));
    }

    // GET /api/stocks/gainers
    @GetMapping("/gainers")
    public ResponseEntity<?> gainers() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    stockService.getTopGainers()
        ));
    }

    // GET /api/stocks/losers
    @GetMapping("/losers")
    public ResponseEntity<?> losers() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    stockService.getTopLosers()
        ));
    }
}