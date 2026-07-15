package com.messenger.service;

import com.messenger.domain.Chat;
import com.messenger.domain.Message;
import com.messenger.domain.User;
import com.messenger.dto.MessageDto;
import com.messenger.exception.ApiException;
import com.messenger.repository.ChatRepository;
import com.messenger.repository.MessageRepository;
import com.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @Transactional
    public MessageDto send(Long chatId, Long senderId, String content) {
        chatService.assertParticipant(chatId, senderId);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Чат не найден"));
        User sender = userRepository.getReferenceById(senderId);

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .build();
        message = messageRepository.save(message);

        chat.setLastMessageText(content);
        chat.setLastMessageAt(Instant.now());
        chatRepository.save(chat);

        return MessageDto.from(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> getHistory(Long chatId, Long userId, int page, int size) {
        chatService.assertParticipant(chatId, userId);
        return messageRepository
                .findByChatIdOrderBySentAtDesc(chatId, PageRequest.of(page, size))
                .map(MessageDto::from);
    }
}
