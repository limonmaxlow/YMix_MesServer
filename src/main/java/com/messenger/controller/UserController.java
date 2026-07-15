package com.messenger.controller;

import com.messenger.dto.UserDto;
import com.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ChatService chatService;
    private final CurrentUser currentUser;

    // Поиск пользователей по имени, чтобы начать новый личный чат
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(chatService.searchUsers(query, currentUser.id()));
    }
}
