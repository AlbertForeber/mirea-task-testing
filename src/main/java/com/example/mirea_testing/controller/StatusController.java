package com.example.mirea_testing.controller;

import com.example.mirea_testing.exception.UserNotFoundException;
import com.example.mirea_testing.service.StatusService;
import com.example.mirea_testing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * Свой и чужой статус разведены по разным путям намеренно.
 * Раньше оба варианта висели на /api/status и различались условием
 * params = "username". Для Spring это разные обработчики, а для OpenAPI —
 * одна ячейка paths./api/status.get, потому что операция там адресуется
 * только парой «путь + метод». Из-за этого половина методов молча
 * пропадала из документации.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Status", description = "Пользовательские статусы. Все методы требуют JWT.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(
                responseCode = "401",
                description = "Токен отсутствует, протух или повреждён",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Нужна роль admin",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
})
public class StatusController {

    private final StatusService statusService;
    private final UserService userService;

    public StatusController(StatusService statusService, UserService userService) {
        this.statusService = statusService;
        this.userService = userService;
    }

    @Operation(
            summary = "Свой статус",
            description = "Логин берётся из токена, поэтому параметров у метода нет."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Текущий статус владельца токена",
            content = @Content(schema = @Schema(example = "{\"status\": \"This is default status\"}"))
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(Collections.singletonMap("state", statusService.getStatus(username)));
    }

    @Operation(
            summary = "Изменить свой статус",
            description = "Логин берётся из токена."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Статус обновлён",
            content = @Content(schema = @Schema(example = "{\"status\": \"on vacation\"}"))
    )
    @PatchMapping("/status")
    public ResponseEntity<Map<String, String>> patchStatus(
            @AuthenticationPrincipal String username,
            @Parameter(description = "Новый статус", example = "on vacation")
            @RequestParam String status
    ) {
        statusService.upsertStatus(username, status);
        return ResponseEntity.ok(Collections.singletonMap("status", statusService.getStatus(username)));
    }

    @Operation(summary = "Статус любого пользователя (только admin)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус запрошенного пользователя",
                    content = @Content(schema = @Schema(example = "{\"status\": \"on vacation\"}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Такого пользователя нет",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/users/{username}/status")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<Map<String, String>> getUserStatus(
            @Parameter(description = "Чей статус смотрим", example = "simple_user")
            @PathVariable String username
    ) {
        requireExistingUser(username);
        return ResponseEntity.ok(Collections.singletonMap("state", statusService.getStatus(username)));
    }

    @Operation(summary = "Изменить статус любого пользователя (только admin)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус обновлён",
                    content = @Content(schema = @Schema(example = "{\"status\": \"on vacation\"}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Такого пользователя нет",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PatchMapping("/users/{username}/status")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, String>> patchUserStatus(
            @Parameter(description = "Кому меняем статус", example = "simple_user")
            @PathVariable String username,
            @Parameter(description = "Новый статус", example = "on vacation")
            @RequestParam String status
    ) {
        requireExistingUser(username);
        statusService.upsertStatus(username, status);

        return ResponseEntity.ok(Collections.singletonMap("status", statusService.getStatus(username)));
    }

    /**
     * Оба админских метода отвечают на несуществующий логин одинаково — 404.
     * Утечки существования логинов тут нет: они закрыты hasRole('admin').
     */
    private void requireExistingUser(String username) {
        userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
