package com.stockpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal previousClose;

    @Column(precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal changePercent = BigDecimal.ZERO;

    private Long volume;

    private String sector;

    private String marketCap;

    private String exchange;

    private String iconCode;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}