package com.messenger.service;

import com.messenger.domain.User;
import com.messenger.dto.AuthResponse;
import com.messenger.dto.LoginRequest;
import com.messenger.dto.RegisterRequest;
import com.messenger.dto.UserDto;
import com.messenger.exception.ApiException;
import com.messenger.repository.UserRepository;
import com.messenger.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Пользователь с таким именем уже существует");
        }

        User user = User.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .online(false)
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getId(), user.getUsername());
        return AuthResponse.of(token, jwtService.getAccessTokenTtlSeconds(), UserDto.from(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

        String token = jwtService.generateAccessToken(user.getId(), user.getUsername());
        return AuthResponse.of(token, jwtService.getAccessTokenTtlSeconds(), UserDto.from(user));
    }
}
