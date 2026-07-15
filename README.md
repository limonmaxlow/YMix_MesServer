# Y MIX — Backend

## О проекте

**Y MIX Backend** — серверная часть мессенджера Y MIX: REST API + WebSocket
(STOMP) сервер на Spring Boot, обслуживающий Flutter-клиент. Отвечает за
регистрацию и авторизацию пользователей, хранение переписки, список чатов
и доставку сообщений в реальном времени.

Реализовано:
- Регистрация и вход по логину/паролю, авторизация через JWT (access-токен).
- Личные (1-на-1) чаты: создание/получение, список чатов пользователя с
  последним сообщением и счётчиком непрочитанных.
- Поиск пользователей для начала нового чата.
- История сообщений с пагинацией.
- Обмен сообщениями в реальном времени по WebSocket/STOMP, включая
  персональные уведомления об обновлении списка чатов.
- Отметка чата как прочитанного.

**Стек:** Java 21 · Spring Boot 3.3 · Spring Security + JWT (jjwt) ·
Spring WebSocket/STOMP · Spring Data JPA · Microsoft SQL Server
(`mssql-jdbc`) · Lombok · Maven.

---

## Структура проекта

```
src/main/java/com/messenger/
├── MessengerBackendApplication.java   # точка входа
├── config/         # Security, JWT-фильтр, WebSocket, STOMP auth
├── controller/      # REST-контроллеры (auth, users, chats)
├── domain/          # JPA-сущности: User, Chat, ChatParticipant, Message...
├── dto/             # DTO для запросов/ответов
├── exception/        # ApiException + глобальный обработчик ошибок
├── repository/       # Spring Data JPA репозитории
├── security/         # JwtService, UserDetailsService, UserPrincipal
├── service/          # бизнес-логика: AuthService, ChatService, MessageService
└── ws/               # ChatWebSocketController — STOMP-обработчик сообщений

src/main/resources/application.yml   # конфигурация (БД, JWT, логирование)
docker-compose.yml                    # опциональный SQL Server в Docker
init-db.sql                           # создание БД и пользователя
```

---

## Запуск на Windows 11 (свой установленный SQL Server)

### 1. Создать базу и пользователя
Откройте `init-db.sql` в SSMS / Azure Data Studio под учёткой-администратором
и выполните. Скрипт создаёт БД `messenger`, логин `messenger_app` и выдаёт
ему права на эту БД.

### 2. Проверить TCP/IP и порт
Если SQL Server стоял «из коробки» (особенно Express-редакция), TCP/IP может
быть выключен:
- **Пуск → SQL Server Configuration Manager**
- `SQL Server Network Configuration` → `Protocols for <ИмяИнстанса>` →
  `TCP/IP` → **Enabled**
- Вкладка `IP Addresses` → внизу `IPAll` → `TCP Port` (обычно `1433`)
- После изменений перезапустите службу SQL Server

Если у вас **именованный инстанс** (например `localhost\SQLEXPRESS`),
измените `DB_URL` на `jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=messenger;...`
— порт в этом случае обычно не нужен.

### 3. Настроить строку подключения
По умолчанию `application.yml` уже указывает на `localhost:1433`,
пользователя `messenger_app` и пароль `StrongPass123!` — если использовали
`init-db.sql` как есть, менять ничего не нужно.

Для других значений задайте переменные окружения перед запуском
(PowerShell), не редактируя `application.yml`:
```powershell
$env:DB_URL = "jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=messenger;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = "messenger_app"
$env:DB_PASSWORD = "StrongPass123!"
```

### 4. Установить JDK 21 и Maven
Проверить, что уже стоит:
```powershell
java -version
mvn -version
```
Если нет:
```powershell
winget install Microsoft.OpenJDK.21
winget install Apache.Maven
```
После установки откройте новое окно PowerShell, чтобы подтянулся `PATH`.

### 5. Запустить бэкенд
В папке `messenger-backend`:
```powershell
mvn spring-boot:run
```
Первый запуск скачает зависимости — может занять пару минут. Hibernate сам
создаст таблицы в БД `messenger` (`ddl-auto: update`).

Сервер поднимется на `http://localhost:8080`. Проверка:
```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"ivan\",\"password\":\"123456\",\"displayName\":\"Иван\"}'
```
Если в ответе пришёл `accessToken` — бэкенд поднят и подключён к SQL Server.

> Ошибка подключения (`The TCP/IP connection to the host has failed` /
> `Login failed`) почти всегда означает выключенный TCP/IP (шаг 2) или
> неверные логин/пароль/имя инстанса (шаг 3). Также проверьте, не блокирует
> ли брандмауэр Windows порт 1433.

---

## Запуск в VS Code

### 1. Расширения (один раз)
- **Extension Pack for Java** (Microsoft)
- **Spring Boot Extension Pack** (VMware/Broadcom)

### 2. Открыть проект
`File → Open Folder…` → папка `messenger-backend` (там, где лежит `pom.xml`).
Дождитесь, пока в статус-баре пройдёт «Importing Java projects…» — Maven
подтягивает зависимости.

### 3. Запустить (любой способ)
- Откройте `MessengerBackendApplication.java` → над методом `main` нажмите
  `Run` (или `Debug`)
- Или через **Spring Boot Dashboard** (иконка листика слева) → ▶️
- Или через терминал: `mvn spring-boot:run`

В `.vscode/launch.json` уже прописаны переменные окружения для БД — при
другом инстансе/пароле поправьте их там.

Сервер поднимется на `http://localhost:8080`. Остановить — ⏹ на панели
отладки или `Ctrl+C` в терминале.

---

<details>
<summary>Альтернатива: SQL Server через Docker</summary>

```bash
docker compose up -d
```
Поднимет SQL Server 2022 на `localhost:1433`, `sa` / `YourStrong!Passw0rd` —
см. `docker-compose.yml`. Далее используйте `init-db.sql` так же, как
описано выше (например, через `sqlcmd` внутри контейнера).
</details>

---

## REST API

### Авторизация
```
POST /api/auth/register
{ "username": "ivan", "password": "123456", "displayName": "Иван" }

POST /api/auth/login
{ "username": "ivan", "password": "123456" }
```
Ответ:
```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "user": { "id": 1, "username": "ivan", "displayName": "Иван", "online": false, "lastSeenAt": null }
}
```
Все остальные запросы (REST и WS) требуют заголовок:
`Authorization: Bearer <accessToken>`

### Пользователи
```
GET /api/users/search?q=iv    -> поиск пользователей для создания чата
```

### Чаты
```
GET  /api/chats                                    -> список чатов текущего пользователя
POST /api/chats/private                             -> создать/получить личный чат
     { "username": "petya" }
GET  /api/chats/{chatId}/messages?page=0&size=30    -> история сообщений (новые сначала)
POST /api/chats/{chatId}/read                        -> отметить прочитанным
     { "lastReadMessageId": 42 }
```

`ChatDto` содержит `otherUser`, `lastMessageText`, `lastMessageAt`,
`unreadCount` — этого достаточно, чтобы отрисовать список чатов одним
запросом.

---

## WebSocket (STOMP)

Endpoint: `ws://localhost:8080/ws` (raw STOMP, подходит для
`stomp_dart_client`). Для отладки из браузера — SockJS-вариант:
`http://localhost:8080/ws-sockjs`.

При STOMP CONNECT нужно передать заголовок:
```
Authorization: Bearer <accessToken>
```

### Отправка сообщения
Клиент шлёт `SEND` на `/app/chat.send`:
```json
{ "chatId": 1, "content": "Привет!" }
```

### Получение сообщений
1. `/topic/chat.{chatId}` — новые сообщения открытого чата.
2. `/user/queue/chats` — обновлённый `ChatDto` (последнее сообщение,
   счётчик непрочитанных) при получении сообщения в любом чате пользователя.

Пример на Dart (`stomp_dart_client`):
```dart
StompClient(
  config: StompConfig(
    url: 'ws://<host>:8080/ws',
    stompConnectHeaders: {'Authorization': 'Bearer $accessToken'},
    onConnect: (frame) {
      stompClient.subscribe(
        destination: '/user/queue/chats',
        callback: (frame) => print(frame.body),
      );
      stompClient.subscribe(
        destination: '/topic/chat.$chatId',
        callback: (frame) => print(frame.body),
      );
    },
  ),
);

stompClient.send(
  destination: '/app/chat.send',
  body: jsonEncode({'chatId': chatId, 'content': text}),
);
```

---

## Что дальше (не реализовано, но легко добавить)
- Групповые чаты (`ChatType.GROUP`, добавление/удаление участников)
- Онлайн-статусы через WS (события подключения/отключения, поле `User.online`)
- Push-уведомления, вложения/файлы, редактирование/удаление сообщений
- Refresh-токены (сейчас только access-токен на 60 минут — см. `app.jwt`
  в `application.yml`)

## Важно перед продакшеном
- Смените `app.jwt.secret` на свой секрет и вынесите его в переменную
  окружения
- Смените пароль `messenger_app` в SQL Server, вынесите креды датасорса в
  переменные окружения (`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`)
- Сузьте CORS (`SecurityConfig.corsConfigurationSource`) до реальных
  адресов клиента
- Для теста/CI можно оставить H2 (уже в `pom.xml`, `scope=test`) и настроить
  отдельный `application-test.yml`
