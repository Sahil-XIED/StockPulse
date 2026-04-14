package com.stockpulse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User entity — maps to `users` table in MySQL
 */
@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;   // BCrypt hashed

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = new BigDecimal("100000.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RiskProfile riskProfile = RiskProfile.MODERATE;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { USER, ADMIN }

    public enum RiskProfile { CONSERVATIVE, MODERATE, AGGRESSIVE }
}
