package com.stockpulse.config;

import com.stockpulse.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

/**
 * SecurityConfig — Spring Security 6 configuration.
 * Stateless JWT, CORS for VS Code Live Server, BCrypt.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private UserDetailsService userDetailsService;

    // ── Security Filter Chain ───────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigSource()))
            .sessionManagement(sess ->
                    sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no JWT needed
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/stocks").permitAll()
                .requestMatchers("/api/stocks/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Admin only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else needs authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── CORS — allow VS Code Live Server origins ────────────
    @Bean
    public CorsConfigurationSource corsConfigSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // ── Password Encoder ────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ── Authentication Manager ──────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── DAO Auth Provider ────────────────────────────────────
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
