package com.stockpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ═══════════════════════════════════════════════════════════
//  Watchlist.java
// ═══════════════════════════════════════════════════════════
@Entity
@Table(name = "watchlist",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","symbol"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
