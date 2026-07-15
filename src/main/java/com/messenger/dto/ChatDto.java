package com.messenger.dto;

import java.time.Instant;

public record ChatDto(
        Long id,
        String type,
        UserDto otherUser,
        String lastMessageText,
        Instant lastMessageAt,
        long unreadCount
) {}
