package com.stockpulse.service;

import com.stockpulse.exception.StockPulseException;
import com.stockpulse.model.*;
import com.stockpulse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepo;
    private final StockRepository     stockRepo;
    private final UserRepository      userRepo;

    /** Returns watchlist items enriched with current stock data */
    public List<Map<String, Object>> getWatchlist(Long userId) {
        List<Watchlist> entries = watchlistRepo.findByUserIdOrderByAddedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Watchlist w : entries) {
            Stock stock = stockRepo.findBySymbol(w.getSymbol()).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",          w.getId());
            item.put("symbol",      w.getSymbol());
            item.put("addedAt",     w.getAddedAt());
            if (stock != null) {
                item.put("companyName",   stock.getCompanyName());
                item.put("price",         stock.getPrice());
                item.put("previousClose", stock.getPreviousClose());
                item.put("changePercent", stock.getChangePercent());
                item.put("sector",        stock.getSector());
                item.put("iconCode",      stock.getIconCode());
            }
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void addToWatchlist(Long userId, String symbol) {
        if (watchlistRepo.existsByUserIdAndSymbol(userId, symbol.toUpperCase())) {
            throw new StockPulseException(symbol + " is already in your watchlist");
        }
        if (!stockRepo.findBySymbol(symbol.toUpperCase()).isPresent()) {
            throw new StockPulseException("Stock not found: " + symbol);
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new StockPulseException("User not found"));
        watchlistRepo.save(Watchlist.builder().user(user).symbol(symbol.toUpperCase()).build());
        log.info("Added {} to watchlist for userId={}", symbol, userId);
    }

    @Transactional
    public void removeFromWatchlist(Long userId, String symbol) {
        watchlistRepo.deleteByUserIdAndSymbol(userId, symbol.toUpperCase());
        log.info("Removed {} from watchlist for userId={}", symbol, userId);
    }

    public boolean isWatched(Long userId, String symbol) {
        return watchlistRepo.existsByUserIdAndSymbol(userId, symbol.toUpperCase());
    }
}
