package com.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "только латиница, цифры и _")
        String username,

        @NotBlank @Size(min = 6, max = 72)
        String password,

        @NotBlank @Size(min = 1, max = 64)
        String displayName
) {}
