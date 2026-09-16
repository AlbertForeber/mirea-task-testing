package com.example.mirea_testing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Данные для входа")
public class LoginRequest {

    @Schema(description = "Логин", example = "simple_user")
    @NotBlank
    private String username;

    @Schema(description = "Пароль", example = "test")
    @NotBlank
    private String password;
}
