package com.stockpulse.service;

import com.stockpulse.exception.StockPulseException;
import com.stockpulse.model.Portfolio;
import com.stockpulse.model.Stock;
import com.stockpulse.model.User;
import com.stockpulse.repository.PortfolioRepository;
import com.stockpulse.repository.StockRepository;
import com.stockpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepo;
    private final StockRepository     stockRepo;
    private final UserRepository      userRepo;

    public Map<String, Object> getPortfolioSummary(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new StockPulseException("User not found: " + userId));

        List<Portfolio> holdings = portfolioRepo.findByUserId(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrent  = BigDecimal.ZERO;
        List<Map<String, Object>> holdingDetails = new ArrayList<>();

        for (Portfolio h : holdings) {
            Stock stock = stockRepo.findBySymbol(h.getSymbol()).orElse(null);
            if (stock == null) continue;

            BigDecimal invested = h.getAveragePrice()
                    .multiply(BigDecimal.valueOf(h.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal current = stock.getPrice()
                    .multiply(BigDecimal.valueOf(h.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal pnl    = current.subtract(invested);
            BigDecimal pnlPct = invested.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : pnl.divide(invested, 4, RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100))
                         .setScale(2, RoundingMode.HALF_UP);

            totalInvested = totalInvested.add(invested);
            totalCurrent  = totalCurrent.add(current);

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("symbol",           h.getSymbol());
            detail.put("companyName",      stock.getCompanyName());
            detail.put("sector",           stock.getSector());
            detail.put("iconCode",         stock.getIconCode());
            detail.put("quantity",         h.getQuantity());
            detail.put("averagePrice",     h.getAveragePrice());
            detail.put("currentPrice",     stock.getPrice());
            detail.put("investedValue",    invested);
            detail.put("currentValue",     current);
            detail.put("unrealizedPnL",    pnl);
            detail.put("unrealizedPnLPct", pnlPct);
            holdingDetails.add(detail);
        }

        BigDecimal totalPnL    = totalCurrent.subtract(totalInvested);
        BigDecimal totalPnLPct = totalInvested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalPnL.divide(totalInvested, 4, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                           .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId",            userId);
        result.put("availableBalance",  user.getBalance());
        result.put("totalInvested",     totalInvested);
        result.put("totalCurrentValue", totalCurrent);
        result.put("totalPnL",          totalPnL);
        result.put("totalPnLPercent",   totalPnLPct);
        result.put("totalHoldings",     holdings.size());
        result.put("holdings",          holdingDetails);
        return result;
    }
}