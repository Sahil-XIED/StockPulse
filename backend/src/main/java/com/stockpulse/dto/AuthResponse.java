package com.stockpulse.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String     token;
    private Long       id;
    private String     name;
    private String     email;
    private String     role;
    private BigDecimal balance;
}