package com.runners.app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DevTokenRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 100) String name,
        @Size(max = 50) String role
) {}

