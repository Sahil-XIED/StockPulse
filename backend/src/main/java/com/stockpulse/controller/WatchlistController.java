package com.stockpulse.controller;

import com.stockpulse.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    // GET /api/watchlist/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<?> getWatchlist(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    watchlistService.getWatchlist(userId)
        ));
    }

    // POST /api/watchlist/add
    @PostMapping("/add")
    public ResponseEntity<?> addToWatchlist(@RequestBody Map<String, Object> body) {
        Long   userId = Long.parseLong(body.get("userId").toString());
        String symbol = body.get("symbol").toString();
        watchlistService.addToWatchlist(userId, symbol);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", symbol + " added to watchlist"
        ));
    }

    // DELETE /api/watchlist/{userId}/{symbol}
    @DeleteMapping("/{userId}/{symbol}")
    public ResponseEntity<?> removeFromWatchlist(
            @PathVariable Long userId,
            @PathVariable String symbol) {
        watchlistService.removeFromWatchlist(userId, symbol);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", symbol + " removed from watchlist"
        ));
    }
}