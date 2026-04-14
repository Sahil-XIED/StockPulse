package com.stockpulse.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long       orderId;
    private String     symbol;
    private String     type;
    private Integer    quantity;
    private BigDecimal executedPrice;
    private BigDecimal totalValue;
    private BigDecimal remainingBalance;
    private String     status;
    private LocalDateTime timestamp;
}