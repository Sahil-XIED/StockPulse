package com.stockpulse.service;

import com.stockpulse.exception.StockPulseException;
import com.stockpulse.model.*;
import com.stockpulse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final UserRepository      userRepo;
    private final StockRepository     stockRepo;
    private final OrderRepository     orderRepo;
    private final PortfolioRepository portfolioRepo;

    // ── BUY ─────────────────────────────────────────────────
    @Transactional
    public Order buy(Long userId, String symbol, int qty) {
        User  user  = getUser(userId);
        Stock stock = getStock(symbol);

        BigDecimal cost = stock.getPrice()
                .multiply(BigDecimal.valueOf(qty))
                .setScale(2, RoundingMode.HALF_UP);

        if (user.getBalance().compareTo(cost) < 0) {
            throw new StockPulseException(
                "Insufficient balance. Required: ₹" + cost + ", Available: ₹" + user.getBalance());
        }

        // Deduct balance
        user.setBalance(user.getBalance().subtract(cost));
        userRepo.save(user);

        // Update portfolio (average price calculation)
        Portfolio holding = portfolioRepo
                .findByUserIdAndSymbol(userId, symbol)
                .orElse(Portfolio.builder().user(user).symbol(symbol).quantity(0)
                        .averagePrice(BigDecimal.ZERO).build());

        BigDecimal totalCost = holding.getAveragePrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()))
                .add(cost);
        holding.setQuantity(holding.getQuantity() + qty);
        holding.setAveragePrice(totalCost.divide(
                BigDecimal.valueOf(holding.getQuantity()), 2, RoundingMode.HALF_UP));
        portfolioRepo.save(holding);

        // Record order
        Order order = Order.builder()
                .user(user).symbol(symbol)
                .quantity(qty).price(stock.getPrice())
                .type(Order.OrderType.BUY).status(Order.OrderStatus.COMPLETED)
                .build();
        Order saved = orderRepo.save(order);
        log.info("BUY order: {} × {} @ ₹{} for userId={}", qty, symbol, stock.getPrice(), userId);
        return saved;
    }

    // ── SELL ────────────────────────────────────────────────
    @Transactional
    public Order sell(Long userId, String symbol, int qty) {
        User  user  = getUser(userId);
        Stock stock = getStock(symbol);

        Portfolio holding = portfolioRepo.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new StockPulseException("You don't hold any " + symbol + " shares"));

        if (holding.getQuantity() < qty) {
            throw new StockPulseException(
                "Insufficient shares. You hold " + holding.getQuantity() + ", requested: " + qty);
        }

        BigDecimal saleValue = stock.getPrice()
                .multiply(BigDecimal.valueOf(qty))
                .setScale(2, RoundingMode.HALF_UP);

        // Credit balance
        user.setBalance(user.getBalance().add(saleValue));
        userRepo.save(user);

        // Update or remove portfolio entry
        holding.setQuantity(holding.getQuantity() - qty);
        if (holding.getQuantity() == 0) {
            portfolioRepo.delete(holding);
        } else {
            portfolioRepo.save(holding);
        }

        // Record order
        Order order = Order.builder()
                .user(user).symbol(symbol)
                .quantity(qty).price(stock.getPrice())
                .type(Order.OrderType.SELL).status(Order.OrderStatus.COMPLETED)
                .build();
        Order saved = orderRepo.save(order);
        log.info("SELL order: {} × {} @ ₹{} for userId={}", qty, symbol, stock.getPrice(), userId);
        return saved;
    }

    // ── Order History ────────────────────────────────────────
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepo.findByUserIdOrderByTimestampDesc(userId);
    }

    // ── Helpers ─────────────────────────────────────────────
    private User getUser(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new StockPulseException("User not found: " + id));
    }

    private Stock getStock(String symbol) {
        return stockRepo.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new StockPulseException("Stock not found: " + symbol));
    }
}
