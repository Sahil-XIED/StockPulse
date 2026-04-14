package com.stockpulse.controller;

import com.stockpulse.dto.OrderRequest;
import com.stockpulse.model.Order;
import com.stockpulse.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders/buy
    @PostMapping("/buy")
    public ResponseEntity<?> buy(@Valid @RequestBody OrderRequest req) {
        Order order = orderService.buy(req.getUserId(), req.getSymbol(), req.getQuantity());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Buy order placed successfully",
                "data",    order
        ));
    }

    // POST /api/orders/sell
    @PostMapping("/sell")
    public ResponseEntity<?> sell(@Valid @RequestBody OrderRequest req) {
        Order order = orderService.sell(req.getUserId(), req.getSymbol(), req.getQuantity());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Sell order placed successfully",
                "data",    order
        ));
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> userOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data",    orders
        ));
    }
}