package com.stockpulse.controller;

import com.stockpulse.repository.UserRepository;
import com.stockpulse.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final StockService   stockService;

    // GET /api/admin/users  (ADMIN only — secured in SecurityConfig)
    @GetMapping("/users")
    public ResponseEntity<?> allUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",        u.getId());
                    m.put("name",      u.getName());
                    m.put("email",     u.getEmail());
                    m.put("role",      u.getRole());
                    m.put("balance",   u.getBalance());
                    m.put("createdAt", u.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "data", users));
    }

    // GET /api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers",  userRepository.count());
        stats.put("totalStocks", stockService.getAllStocks().size());
        return ResponseEntity.ok(Map.of("success", true, "data", stats));
    }
}