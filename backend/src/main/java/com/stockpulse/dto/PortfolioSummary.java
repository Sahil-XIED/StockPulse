package com.stockpulse.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSummary {

    private BigDecimal           totalInvested;
    private BigDecimal           totalCurrentValue;
    private BigDecimal           totalPnL;
    private BigDecimal           totalPnLPercent;
    private BigDecimal           availableBalance;
    private Integer              totalHoldings;
    private List<PortfolioHolding> holdings;
}