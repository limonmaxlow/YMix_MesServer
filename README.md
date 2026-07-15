# Messenger Backend (Java 21 + Spring Boot + WebSocket/STOMP)

Бэкенд для мессенджера: авторизация (JWT), список личных чатов, обмен сообщениями в реальном времени.

## Стек
- Java 21, Spring Boot 3.3
- Spring Security + JWT (jjwt)
- Spring WebSocket + STOMP (совместимо с `stomp_dart_client` во Flutter)
- Spring Data JPA + **Microsoft SQL Server** (драйвер `mssql-jdbc`)

## Запуск на Windows 11 (свой установленный SQL Server)

### 1. Создать базу и пользователя
Откройте `init-db.sql` в SSMS / Azure Data Studio (или вставьте туда, где обычно пишете запросы) и выполните под учёткой-администратором. Скрипт создаёт БД `messenger`, логин `messenger_app` и выдаёт ему права на эту БД.

### 2. Проверить TCP/IP и порт
Если SQL Server стоял "из коробки" (особенно Express-редакция), TCP/IP может быть выключен:
- **Пуск → SQL Server Configuration Manager**
- `SQL Server Network Configuration` → `Protocols for <ИмяИнстанса>` → `TCP/IP` → **Enabled**
- Вкладка `IP Addresses` → низ списка `IPAll` → `TCP Port` (обычно `1433`)
- Если меняли что-то — перезапустите службу SQL Server (`SQL Server (MSSQLSERVER)` в `Службы Windows`, или через ту же Configuration Manager)

Если у вас **именованный инстанс** (например `localhost\SQLEXPRESS`), поменяйте `DB_URL` (см. ниже) на `jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=messenger;...` — порт в этом случае обычно не нужен.

### 3. Настроить строку подключения
По умолчанию `application.yml` уже указывает на `localhost:1433`, пользователя `messenger_app` и пароль `StrongPass123!` — если вы использовали `init-db.sql` как есть, менять ничего не нужно.

Если хотите другие значения — не редактируйте `application.yml`, а задайте переменные окружения перед запуском (PowerShell):
```powershell
$env:DB_URL = "jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=messenger;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = "messenger_app"
$env:DB_PASSWORD = "StrongPass123!"
```

### 4. Установить JDK 21 и Maven (если ещё нет)
Проверить, что уже стоит:
```powershell
java -version
mvn -version
```
Если нет — проще всего через [winget](https://learn.microsoft.com/windows/package-manager/winget/) или choco:
```powershell
winget install Microsoft.OpenJDK.21
winget install Apache.Maven
```
После установки откройте новое окно PowerShell (чтобы подтянулся `PATH`).

### 5. Запустить бэкенд
Распакуйте архив, откройте PowerShell в папке `messenger-backend` и выполните:
```powershell
mvn spring-boot:run
```
Первый запуск скачает зависимости — может занять пару минут. Дальше Hibernate сам создаст таблицы в БД `messenger` (`ddl-auto: update`).

Сервер поднимется на `http://localhost:8080`. Проверить, что всё работает:
```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"ivan\",\"password\":\"123456\",\"displayName\":\"Иван\"}'
```
Если в ответе пришёл `accessToken` — бэкенд поднят и подключён к вашему SQL Server.

> Если ловите ошибку подключения (`The TCP/IP connection to the host has failed` / `Login failed`) — почти всегда это или выключенный TCP/IP (шаг 2), или неверный логин/пароль/имя инстанса (шаг 3). Firewall Windows тоже может блокировать порт 1433 — при необходимости добавьте разрешающее правило.

---

## Запуск в VS Code

### 1. Расширения (один раз)
Установите (Extensions → поиск):
- **Extension Pack for Java** (от Microsoft) — Maven-проект, компиляция, дебаг
- **Spring Boot Extension Pack** (от VMware/Broadcom) — даёт Spring Boot Dashboard и подсветку `application.yml`

### 2. Открыть проект
`File → Open Folder…` → выберите папку `messenger-backend` (ту, что внутри распакованного архива — там, где лежит `pom.xml`).

Подождите, пока в статус-баре внизу пройдёт «Importing Java projects…» / «Building workspace» — это Maven-расширение подтягивает зависимости (первый раз может занять пару минут).

### 3. Запустить
Любой из трёх вариантов — все делают одно и то же:

- **Через код**: откройте `src/main/java/com/messenger/MessengerBackendApplication.java`, над методом `main` появится ссылка `Run | Debug` — нажмите `Run` (или `Debug`, если хотите ставить breakpoints)
- **Через Spring Boot Dashboard**: иконка листика в левой панели → в списке `messenger-backend` → кнопка ▶️ рядом
- **Через встроенный терминал** (`` Ctrl+` ``): `mvn spring-boot:run`

В `.vscode/launch.json` (уже в архиве) прописаны переменные окружения для подключения к БД (`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`) — совпадают со значениями из `init-db.sql`. Если у вас именованный инстанс SQL Server или другой пароль — поправьте их прямо в этом файле, тогда режимы `Run`/`Debug` из VS Code подхватят правильные настройки.

> Если запускаете через терминал (`mvn spring-boot:run`) вместо `Run`/`Debug` из редактора — `launch.json` не используется, задайте переменные окружения в самом терминале (см. шаг 3 в разделе выше) или просто оставьте значения по умолчанию из `application.yml`.

Готово: сервер поднимется на `http://localhost:8080`, консоль вывода — во вкладке `DEBUG CONSOLE` (или `TERMINAL`, если запускали через `mvn`). Остановить — красный квадратик ⏹ на панели отладки или `Ctrl+C` в терминале.

---

<details>
<summary>Альтернатива: поднять SQL Server через Docker (если решите не использовать локально установленный)</summary>

```bash
docker compose up -d
```
Поднимет SQL Server 2022 на `localhost:1433`, `sa` / `YourStrong!Passw0rd` — см. `docker-compose.yml`. В этом случае используйте `init-db.sql` так же, как описано выше, либо прогоните его через `sqlcmd` внутри контейнера.
</details>

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

### Чаты (меню)
```
GET  /api/chats                        -> список чатов текущего пользователя
POST /api/chats/private                -> создать/получить личный чат
     { "username": "petya" }
GET  /api/chats/{chatId}/messages?page=0&size=30   -> история сообщений (новые сначала)
POST /api/chats/{chatId}/read          -> отметить прочитанным
     { "lastReadMessageId": 42 }
```

`ChatDto` содержит `otherUser` (собеседник), `lastMessageText`, `lastMessageAt`, `unreadCount` — этого достаточно, чтобы отрисовать список чатов в Flutter одним запросом.

## WebSocket (STOMP)

Endpoint: `ws://localhost:8080/ws` (raw STOMP over WebSocket — подходит для `stomp_dart_client`).
Для отладки из браузера есть SockJS-вариант: `http://localhost:8080/ws-sockjs`.

При подключении (STOMP CONNECT) нужно передать заголовок:
```
Authorization: Bearer <accessToken>
```

### Отправка сообщения
Клиент шлёт `SEND` на `/app/chat.send`:
```json
{ "chatId": 1, "content": "Привет!" }
```

### Получение сообщений
1. Подписаться на топик конкретного открытого чата (пока он открыт на экране):
   `/topic/chat.{chatId}` — прилетают все новые сообщения этого чата.
2. Подписаться на личную очередь для обновления списка чатов (даже если чат не открыт):
   `/user/queue/chats` — прилетает обновлённый `ChatDto` (последнее сообщение, счётчик непрочитанных) при получении нового сообщения в любом из чатов пользователя.

Пример на псевдо-Dart (`stomp_dart_client`):
```dart
StompClient(
  config: StompConfig(
    url: 'ws://<host>:8080/ws',
    stompConnectHeaders: {'Authorization': 'Bearer $accessToken'},
    onConnect: (frame) {
      stompClient.subscribe(
        destination: '/user/queue/chats',
        callback: (frame) => print(frame.body), // обновление списка чатов
      );
      stompClient.subscribe(
        destination: '/topic/chat.$chatId',
        callback: (frame) => print(frame.body), // новое сообщение в открытом чате
      );
    },
  ),
);

stompClient.send(
  destination: '/app/chat.send',
  body: jsonEncode({'chatId': chatId, 'content': text}),
);
```

## Что дальше (не реализовано, но легко добавить на этой базе)
- Групповые чаты (`ChatType.GROUP`, добавление/удаление участников)
- Онлайн-статусы через WS (событие подключения/отключения, поле `User.online`)
- Push-уведомления, вложения/файлы, редактирование/удаление сообщений
- Refresh-токены (сейчас только access-токен на 60 минут — см. `app.jwt` в `application.yml`)

## Важно перед продакшеном
- Смените `app.jwt.secret` в `application.yml` на свой секрет (вынесите в переменную окружения)
- Смените пароль `messenger_app` в SQL Server и вынесите креды датасорса в переменные окружения (`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`)
- Сузьте CORS (`SecurityConfig.corsConfigurationSource`) до реальных адресов клиента
- Для теста/CI можно оставить H2 (уже добавлена в `pom.xml` со `scope=test`) и настроить отдельный `application-test.yml`
