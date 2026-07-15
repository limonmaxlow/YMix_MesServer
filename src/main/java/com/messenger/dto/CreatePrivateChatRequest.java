package com.messenger.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePrivateChatRequest(
        @NotBlank String username
) {}
