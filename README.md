# Y MIX — Backend

## О проекте
Серверная часть мессенджера Y MIX: REST API + WebSocket (STOMP) на Spring
Boot для Flutter-клиента. Регистрация/авторизация по JWT, личные чаты,
история сообщений, обмен сообщениями в реальном времени.

## Архитектура
```
controller/  — REST-контроллеры (auth, users, chats)
ws/          — STOMP-обработчик сообщений (WebSocket)
service/     — бизнес-логика (Auth, Chat, Message)
repository/  — Spring Data JPA
domain/      — сущности БД (User, Chat, Message...)
dto/         — запросы/ответы
security/    — JWT, UserDetails, фильтры
config/      — Security, WebSocket, CORS
```
Клиент ходит в REST за данными и подключается по WebSocket/STOMP (`/ws`)
для сообщений в реальном времени; JWT передаётся в заголовке `Authorization`
как в REST, так и при STOMP CONNECT.

## Стек
Java 21 · Spring Boot 3.3 (Web, Security, WebSocket, Data JPA) · JWT (jjwt)
· Microsoft SQL Server (`mssql-jdbc`) · Lombok · Maven

## Запуск
Нужен установленный SQL Server (создать БД — скриптом `init-db.sql`) и
JDK 21 + Maven.

```powershell
# 1. Создать БД: выполнить init-db.sql в SSMS/Azure Data Studio

# 2. (если нужно поменять адрес/креды БД — иначе шаг не нужен)
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=messenger;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = "messenger_app"
$env:DB_PASSWORD = "StrongPass123!"

# 3. Запустить
mvn spring-boot:run
```
Сервер поднимется на `http://localhost:8080`. Таблицы создаются автоматически
(`ddl-auto: update`). Проверка:
```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"ivan\",\"password\":\"123456\",\"displayName\":\"Иван\"}'
```
Ответ с `accessToken` — бэкенд работает и подключён к БД.
