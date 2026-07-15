package com.messenger.config;
 
import com.messenger.security.JwtService;
import com.messenger.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
 
// Авторизация WebSocket/STOMP: клиент присылает заголовок Authorization в CONNECT фрейме.
// После успешной проверки Principal подключения = username, дальше он доступен
// в @MessageMapping методах и для convertAndSendToUser(...).
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
 
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
 
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        // ВАЖНО: именно getAccessor(...), а не StompHeaderAccessor.wrap(message).
        // wrap(...) создаёт отдельный accessor поверх заголовков сообщения, и
        // accessor.setUser(...) на нём НЕ сохраняется в STOMP-сессии для
        // последующих кадров (SUBSCRIBE/SEND) — именно поэтому principal
        // был null в ChatWebSocketController. getAccessor(...) возвращает
        // "родной" мутируемый accessor, связанный с этим сообщением, и
        // Spring действительно переносит Principal на все следующие кадры
        // той же WebSocket/STOMP-сессии.
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
 
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Отсутствует Authorization заголовок в STOMP CONNECT");
            }
            String token = authHeader.substring("Bearer ".length());
 
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtService.isTokenValid(token, userDetails.getUsername())) {
                throw new IllegalArgumentException("Невалидный или истёкший токен");
            }
 
            UserPrincipal principal = (UserPrincipal) userDetails;
            accessor.setUser(new StompPrincipal(principal.getUsername()));
        }
 
        return message;
    }
 
    public record StompPrincipal(String name) implements java.security.Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}