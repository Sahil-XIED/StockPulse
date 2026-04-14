package com.stockpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ═══════════════════════════════════════════════════════════
//  Portfolio.java
// ═══════════════════════════════════════════════════════════
@Entity
@Table(name = "portfolio",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","symbol"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal averagePrice;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
