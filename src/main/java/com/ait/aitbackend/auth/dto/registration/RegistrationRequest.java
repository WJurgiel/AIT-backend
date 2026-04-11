package com.ait.aitbackend.auth.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "Username cannot be empty!")
        @Size(min = 3, max = 50, message = "Username should contain 3-50 characters")
        String username,

        @NotBlank(message = "Email cannot be empty!")
        @Email(message = "Incorrect email format provided!")
        String email,

        @NotBlank(message = "Password cannot be empty!")
        @Size(min = 8, message = "Password must have at least 8 characters")
        String password
) {
}
