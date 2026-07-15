package com.messenger.dto;

import com.messenger.domain.User;

import java.time.Instant;

public record UserDto(
        Long id,
        String username,
        String displayName,
        boolean online,
        Instant lastSeenAt
) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getDisplayName(), u.isOnline(), u.getLastSeenAt());
    }
}
