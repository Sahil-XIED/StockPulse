package com.stockpulse.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal limitPrice;   // null = market order
}