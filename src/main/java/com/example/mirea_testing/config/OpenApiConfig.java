package com.example.mirea_testing.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Описание API целиком. Схема безопасности объявлена здесь, но глобально
 * не навязывается: /api/login и /api/register публичные, поэтому требование
 * токена вешается точечно — аннотацией @SecurityRequirement на StatusController.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Mirea Testing API",
                version = "0.0.1",
                description = """
                        Учебный сервис: регистрация, вход по JWT и пользовательские статусы.

                        Как попробовать:
                        1. POST /api/register или POST /api/login — получить токен.
                           Сидовые пользователи: simple_user / test (роль user)
                           и admin_user / test (роль admin).
                        2. Нажать Authorize и вставить токен.
                        3. Дёргать /api/status.

                        Ошибки возвращаются в формате RFC 9457 (application/problem+json).
                        """
        ),
        servers = @Server(url = "/", description = "Локальный запуск")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT из ответа /api/login. Вставлять сам токен, без префикса Bearer."
)
public class OpenApiConfig {
}
