package com.messenger.dto;

import com.messenger.domain.Message;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long chatId,
        Long senderId,
        String senderUsername,
        String content,
        String status,
        Instant sentAt
) {
    public static MessageDto from(Message m) {
        return new MessageDto(
                m.getId(),
                m.getChat().getId(),
                m.getSender().getId(),
                m.getSender().getUsername(),
                m.getContent(),
                m.getStatus().name(),
                m.getSentAt()
        );
    }
}
