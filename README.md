# Mirea Testing API

Учебный сервис на Spring Boot 4: регистрация, вход по JWT и пользовательские статусы.
Данные лежат в памяти (`ConcurrentHashMap`), БД нет - после перезапуска всё сбрасывается
к исходным пользователям.

## Запуск

Нужна только Java 17+ (проверялось на 25). Maven ставить не надо - в репозитории лежит wrapper.

```bash
./mvnw spring-boot:run
```

Приложение поднимется на `http://localhost:8080`.

## Документация

Swagger UI открыт без токена:

**http://localhost:8080/swagger-ui.html**

Сырая спецификация OpenAPI — `http://localhost:8080/v3/api-docs`.

Как попробовать прямо из UI:

1. Раскрыть `POST /api/login`, нажать **Try it out** — логин и пароль уже подставлены примером.
2. Скопировать `token` из ответа.
3. Нажать **Authorize** вверху страницы и вставить туда токен (без слова `Bearer`).
4. Теперь работают методы из раздела **Status**.

## Что есть

Два готовых пользователя, пароль у обоих `test`:

| логин | роль |
|---|---|
| `simple_user` | user |
| `admin_user` | admin |

| метод | путь | кто может |
|---|---|---|
| POST | `/api/register` | все |
| POST | `/api/login` | все |
| GET | `/api/status` | любой с токеном — свой статус |
| PATCH | `/api/status?status=...` | любой с токеном — свой статус |
| GET | `/api/users/{username}/status` | только admin |
| PATCH | `/api/users/{username}/status?status=...` | только admin |

Токен живёт 15 минут (`app.jwt.expiration-time` в `application.yaml`).

## Проверка из консоли

```bash
# войти и сохранить токены
USER=$(curl -s -X POST localhost:8080/api/login -H 'Content-Type: application/json' \
  -d '{"username":"simple_user","password":"test"}' | jq -r .token)
ADMIN=$(curl -s -X POST localhost:8080/api/login -H 'Content-Type: application/json' \
  -d '{"username":"admin_user","password":"test"}' | jq -r .token)

# свой статус
curl -s -H "Authorization: Bearer $USER" localhost:8080/api/status
# {"status":"This is default status"}

# поменять свой статус
curl -s -X PATCH -H "Authorization: Bearer $USER" 'localhost:8080/api/status?status=busy'
# {"status":"busy"}

# админ смотрит чужой
curl -s -H "Authorization: Bearer $ADMIN" localhost:8080/api/users/simple_user/status
# {"status":"busy"}
```

Что должно отработать как ошибка:

```bash
curl -s -H "Authorization: Bearer $USER" localhost:8080/api/users/admin_user/status   # 403
curl -s -H "Authorization: Bearer $ADMIN" localhost:8080/api/users/ghost/status       # 404
curl -s localhost:8080/api/status                                                     # 401
curl -s -H 'Authorization: Bearer garbage' localhost:8080/api/status                  # 401
curl -s -X POST localhost:8080/api/register -H 'Content-Type: application/json' \
  -d '{"username":"ab","password":"1"}'                                               # 400 + список полей
curl -s -X POST localhost:8080/api/register -H 'Content-Type: application/json' \
  -d '{"username":"simple_user","password":"password123"}'                            # 409
```

## Формат ошибок

Все ошибки приходят в виде RFC 9457 (`application/problem+json`), например:

```json
{
  "detail": "Failed to validate some of fields",
  "instance": "/api/register",
  "status": 400,
  "title": "Bad Request",
  "errors": {
    "password": "Password size must be at least 8 characters long",
    "username": "Username size must be at least 3 characters long"
  }
}
```

Поля `type` в ответе нет, хотя в RFC оно есть: Spring не сериализует его, пока оно равно
дефолтному `about:blank`.

Собирается это в двух местах, и разница между ними важна:

- `exception/RestExceptionHandler.java` — ошибки из контроллеров и method security (400, 401, 403, 404, 409, 500).
- `config/SecurityConfig.java` — 401 и 403, возникшие в цепочке фильтров (нет токена, токен протух).
  Туда `@RestControllerAdvice` не достаёт: эти отказы происходят раньше `DispatcherServlet`,
  поэтому для них заданы отдельные `AuthenticationEntryPoint` и `AccessDeniedHandler`.
