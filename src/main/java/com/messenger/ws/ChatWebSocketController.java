package com.messenger.ws;
 
import com.messenger.dto.ChatDto;
import com.messenger.dto.MessageDto;
import com.messenger.dto.SendMessageRequest;
import com.messenger.repository.ChatParticipantRepository;
import com.messenger.repository.UserRepository;
import com.messenger.service.ChatService;
import com.messenger.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
 
import java.security.Principal;
 
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
 
    private final MessageService messageService;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;
 
    // Клиент шлёт STOMP SEND на /app/chat.send с телом SendMessageRequest.
    // Principal.getName() = username, выставленный в StompAuthChannelInterceptor при CONNECT.
    @MessageMapping("/chat.send")
    public void sendMessage(@Valid SendMessageRequest request, Principal principal) {
        if (principal == null) {
            // Если сюда попали - значит STOMP-сессия не аутентифицирована
            // (см. StompAuthChannelInterceptor). Явная ошибка вместо NPE.
            throw new IllegalStateException(
                    "STOMP-сессия не аутентифицирована: principal == null. " +
                    "Проверьте, что клиент передаёт заголовок Authorization в CONNECT-фрейме.");
        }
 
        Long senderId = userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден")).getId();
 
        MessageDto saved = messageService.send(request.chatId(), senderId, request.content());
 
        // 1) рассылаем сообщение всем, кто подписан на топик чата (открытый экран переписки)
        messagingTemplate.convertAndSend("/topic/chat." + request.chatId(), saved);
 
        // 2) уведомляем каждого участника персонально — для обновления списка чатов (последнее сообщение, счётчик)
        chatParticipantRepository.findByChatId(request.chatId()).forEach(participant -> {
            ChatDto chatDto = chatService.toDto(participant.getChat(), participant.getUser().getId());
            messagingTemplate.convertAndSendToUser(
                    participant.getUser().getUsername(),
                    "/queue/chats",
                    chatDto
            );
        });
    }
}