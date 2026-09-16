package com.example.mirea_testing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Данные для регистрации")
public class RegisterRequest {

    @Schema(description = "Логин, минимум 3 символа", example = "new_user", minLength = 3)
    @NotBlank
    @Size(min = 3, message = "Username size must be at least 3 characters long")
    private String username;

    @Schema(description = "Пароль, минимум 8 символов", example = "password123", minLength = 8)
    @NotBlank
    @Size(min = 4, message = "Password size must be at least 8 characters long")
    private String password;
}
