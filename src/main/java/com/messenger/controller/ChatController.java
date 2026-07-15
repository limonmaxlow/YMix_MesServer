package com.messenger.controller;

import com.messenger.dto.ChatDto;
import com.messenger.dto.CreatePrivateChatRequest;
import com.messenger.dto.MessageDto;
import com.messenger.service.ChatService;
import com.messenger.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final CurrentUser currentUser;

    // Список чатов (меню) текущего пользователя
    @GetMapping
    public ResponseEntity<List<ChatDto>> getChats() {
        return ResponseEntity.ok(chatService.getUserChats(currentUser.id()));
    }

    // Создать (или получить существующий) личный чат с пользователем по username
    @PostMapping("/private")
    public ResponseEntity<ChatDto> createPrivateChat(@Valid @RequestBody CreatePrivateChatRequest request) {
        return ResponseEntity.ok(chatService.getOrCreatePrivateChat(currentUser.id(), request.username()));
    }

    // История сообщений чата (пагинация, сначала новые)
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageDto>> getMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(messageService.getHistory(chatId, currentUser.id(), page, size));
    }

    // Отметить чат прочитанным до конкретного id сообщения
    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long chatId, @RequestBody Map<String, Long> body) {
        chatService.markRead(chatId, currentUser.id(), body.get("lastReadMessageId"));
        return ResponseEntity.noContent().build();
    }
}
