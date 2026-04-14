package com.stockpulse.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
       indexes = @Index(name = "idx_orders_user", columnList = "user_id"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.COMPLETED;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum OrderType   { BUY, SELL }
    public enum OrderStatus { COMPLETED, PENDING, FAILED, CANCELLED }
}
