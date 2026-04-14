package com.stockpulse.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}