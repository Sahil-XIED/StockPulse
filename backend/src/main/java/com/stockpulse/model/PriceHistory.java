package com.stockpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history",
       indexes = @Index(name = "idx_ph_symbol_time", columnList = "symbol,recordedAt"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();
}
