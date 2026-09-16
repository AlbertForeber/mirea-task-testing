package com.example.mirea_testing.controller;

import com.example.mirea_testing.dto.LoginRequest;
import com.example.mirea_testing.dto.RegisterRequest;
import com.example.mirea_testing.service.AuthFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Регистрация и вход. Оба эндпоинта публичные — токен для них не нужен.")
public class AuthController {

    private final AuthFacade authFacade;

    @Operation(
            summary = "Вход по логину и паролю",
            description = "Возвращает JWT со сроком жизни 15 минут. "
                    + "Сидовые пользователи: simple_user / test и admin_user / test."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Токен выдан",
                    content = @Content(
                            schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzUxMiJ9...\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Пустой логин или пароль. Список полей — в свойстве errors",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный логин или пароль. Несуществующий логин даёт тот же ответ — "
                            + "чтобы нельзя было перебором собрать список живых аккаунтов",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(Map.of("token", authFacade.login(request)));
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт пользователя с ролью user, выдаёт ему статус по умолчанию "
                    + "и сразу возвращает JWT — повторный вход не нужен."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь создан, токен выдан",
                    content = @Content(
                            schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzUxMiJ9...\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Логин короче 3 символов или пароль короче 8. Список полей — в свойстве errors",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Логин уже занят",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
           @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(Map.of("token", authFacade.register(request)));
    }
}
