package com.messenger.service;

import com.messenger.domain.*;
import com.messenger.dto.ChatDto;
import com.messenger.dto.UserDto;
import com.messenger.exception.ApiException;
import com.messenger.repository.ChatParticipantRepository;
import com.messenger.repository.ChatRepository;
import com.messenger.repository.MessageRepository;
import com.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<ChatDto> getUserChats(Long userId) {
        return chatRepository.findAllForUser(userId).stream()
                .map(chat -> toDto(chat, userId))
                .toList();
    }

    @Transactional
    public ChatDto getOrCreatePrivateChat(Long currentUserId, String otherUsername) {
        User other = userRepository.findByUsernameIgnoreCase(otherUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (other.getId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Нельзя создать чат с самим собой");
        }

        Chat chat = chatRepository.findPrivateChatBetween(currentUserId, other.getId())
                .orElseGet(() -> createPrivateChat(currentUserId, other.getId()));

        return toDto(chat, currentUserId);
    }

    private Chat createPrivateChat(Long userAId, Long userBId) {
        User userA = userRepository.getReferenceById(userAId);
        User userB = userRepository.getReferenceById(userBId);

        Chat chat = Chat.builder().type(ChatType.PRIVATE).build();
        chat = chatRepository.save(chat);

        participantRepository.save(ChatParticipant.builder().chat(chat).user(userA).build());
        participantRepository.save(ChatParticipant.builder().chat(chat).user(userB).build());

        return chat;
    }

    @Transactional(readOnly = true)
    public void assertParticipant(Long chatId, Long userId) {
        if (!participantRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Вы не участник этого чата");
        }
    }

    @Transactional
    public void markRead(Long chatId, Long userId, Long lastReadMessageId) {
        ChatParticipant participant = participantRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Вы не участник этого чата"));
        participant.setLastReadMessageId(lastReadMessageId);
        participantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String query, Long excludeUserId) {
        return userRepository.findTop20ByUsernameContainingIgnoreCaseAndIdNot(query, excludeUserId).stream()
                .map(UserDto::from)
                .toList();
    }

    /** Собирает ChatDto с точки зрения currentUserId (кто "собеседник", сколько непрочитанных). */
    public ChatDto toDto(Chat chat, Long currentUserId) {
        List<ChatParticipant> participants = participantRepository.findByChatId(chat.getId());

        UserDto otherUserDto = participants.stream()
                .map(ChatParticipant::getUser)
                .filter(u -> !u.getId().equals(currentUserId))
                .findFirst()
                .map(UserDto::from)
                .orElse(null);

        Long lastReadId = participants.stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .map(ChatParticipant::getLastReadMessageId)
                .orElse(null);

        long unread = (lastReadId == null)
                ? messageRepository.countByChatId(chat.getId())
                : messageRepository.countByChatIdAndIdGreaterThan(chat.getId(), lastReadId);

        return new ChatDto(
                chat.getId(),
                chat.getType().name(),
                otherUserDto,
                chat.getLastMessageText(),
                chat.getLastMessageAt(),
                unread
        );
    }
}
