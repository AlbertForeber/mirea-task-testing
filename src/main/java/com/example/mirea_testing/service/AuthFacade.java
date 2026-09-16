package com.example.mirea_testing.service;

import com.example.mirea_testing.dto.LoginRequest;
import com.example.mirea_testing.dto.RegisterRequest;
import com.example.mirea_testing.exception.UserAlreadyExistsException;
import com.example.mirea_testing.model.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthFacade {

    private static final String DEFAULT_STATUS = "This is default status";

    private final UserService userService;
    private final StatusService statusService;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;

    public AuthFacade(
            UserService userService,
            StatusService statusService,
            AuthenticationProvider authenticationProvider,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.statusService = statusService;
        this.authenticationProvider = authenticationProvider;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        User user = (User) authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        ).getPrincipal();

        return jwtService.generateToken(user);
    }

    public String register(RegisterRequest request) {
        if (userService.getUserByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        // Статус выдаётся только после успешного создания пользователя.
        // Иначе регистрация на занятый логин затирала бы чужой статус
        // и всё равно заканчивалась отказом.
        User user = userService.createUser(request.getUsername(), request.getPassword());
        statusService.upsertStatus(user.getUsername(), DEFAULT_STATUS);

        return jwtService.generateToken(user);
    }
}
