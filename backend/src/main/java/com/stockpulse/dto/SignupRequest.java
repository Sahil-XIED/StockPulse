package com.stockpulse.dto;

import com.stockpulse.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private User.RiskProfile riskProfile;

	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
	}
}