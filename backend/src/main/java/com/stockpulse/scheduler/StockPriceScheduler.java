package com.stockpulse.scheduler;

import com.stockpulse.model.User;
import com.stockpulse.repository.UserRepository;
import com.stockpulse.service.StockService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceScheduler {

    private final StockService    stockService;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // Simulate live price updates every 5 seconds
    @Scheduled(fixedDelay = 5000)
    public void tickPrices() {
        try {
            stockService.simulatePriceUpdates();
        } catch (Exception e) {
            log.error("Price simulation error: {}", e.getMessage());
        }
    }

    // Seed default admin + demo users on startup
    @PostConstruct
    public void seedUsers() {
        seedUser("Admin User",  "admin@stockpulse.com", "admin123",
                 User.Role.ADMIN, new BigDecimal("9999999.00"));
        seedUser("Demo Trader", "demo@stockpulse.com",  "demo123",
                 User.Role.USER,  new BigDecimal("100000.00"));
    }

    private void seedUser(String name, String email, String rawPassword,
                          User.Role role, BigDecimal balance) {
        if (!userRepository.existsByEmail(email)) {
            User u = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .balance(balance)
                    .riskProfile(User.RiskProfile.MODERATE)
                    .build();
            userRepository.save(u);
            log.info("Seeded user: {} ({})", email, role);
        }
    }
}