package com.stockpulse.service;

import com.stockpulse.dto.AuthResponse;
import com.stockpulse.dto.LoginRequest;
import com.stockpulse.dto.SignupRequest;
import com.stockpulse.exception.StockPulseException;
import com.stockpulse.model.User;
import com.stockpulse.repository.UserRepository;
import com.stockpulse.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;

    // ── SIGNUP ──────────────────────────────────────────────
    @Transactional
    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new StockPulseException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(req.getPassword()))
                .balance(new BigDecimal("100000.00"))
                .role(User.Role.USER)
                .riskProfile(req.getRiskProfile() != null
                        ? req.getRiskProfile()
                        : User.RiskProfile.MODERATE)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());

        String token = jwtUtil.generateToken(saved.getEmail());
        return buildResponse(token, saved);
    }

    // ── LOGIN ───────────────────────────────────────────────
    public AuthResponse login(LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getEmail().toLowerCase().trim(),
                            req.getPassword()
                    )
            );
            String token = jwtUtil.generateToken(auth);
            User user = userRepository.findByEmail(req.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new StockPulseException("User not found"));
            log.info("User logged in: {}", user.getEmail());
            return buildResponse(token, user);

        } catch (BadCredentialsException ex) {
            throw new StockPulseException("Invalid email or password");
        }
    }

    // ── Helper ──────────────────────────────────────────────
    private AuthResponse buildResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .balance(user.getBalance())
                .build();
    }
}