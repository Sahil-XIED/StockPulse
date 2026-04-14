package com.stockpulse.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioHolding {

    private String     symbol;
    private String     companyName;
    private String     sector;
    private String     iconCode;
    private Integer    quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal investedValue;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPnL;
    private BigDecimal unrealizedPnLPercent;
}