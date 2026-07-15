<div align="center">

# 💬 YMix Backend

**Серверная часть мессенджера YMix**

Java 21 · Spring Boot 3.3 · WebSocket/STOMP · JWT · MS SQL Server

</div>

---

## 📖 О проекте

YMix Backend — это REST API и WebSocket (STOMP) сервер на Spring Boot,
который обслуживает Flutter-клиент YMix.

**Возможности:**

- 🔐 Регистрация и авторизация пользователей по JWT
- 💬 Личные чаты (создание, список, история сообщений)
- ⚡ Обмен сообщениями в реальном времени по WebSocket
- ✅ Отметка сообщений прочитанными

---

## 🏗 Архитектура

| Пакет | Назначение |
|---|---|
| `controller/` | REST-контроллеры (auth, users, chats) |
| `ws/` | STOMP-обработчик сообщений (WebSocket) |
| `service/` | бизнес-логика (Auth, Chat, Message) |
| `repository/` | Spring Data JPA |
| `domain/` | сущности БД (User, Chat, Message...) |
| `dto/` | запросы и ответы |
| `security/` | JWT, UserDetails, фильтры |
| `config/` | Security, WebSocket, CORS |

Клиент обращается к REST API за данными и подключается по WebSocket/STOMP
(`/ws`) для сообщений в реальном времени. JWT передаётся в заголовке
`Authorization` как в REST-запросах, так и при STOMP CONNECT.

---

## 🛠 Стек

- Java 21
- Spring Boot 3.3 (Web, Security, WebSocket, Data JPA)
- JWT (jjwt)
- Microsoft SQL Server (`mssql-jdbc`)
- Lombok
- Maven

---

## 🚀 Запуск

Понадобится установленный SQL Server и JDK 21 с Maven.

**1. Создать базу данных**

Выполните `init-db.sql` в SSMS или Azure Data Studio.

**2. При необходимости изменить адрес или креды БД**

```powershell
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=messenger;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME = "messenger_app"
$env:DB_PASSWORD = "StrongPass123!"
```

**3. Запустить приложение**

```powershell
mvn spring-boot:run
```

Сервер поднимется на `http://localhost:8080`, таблицы создадутся
автоматически.

**Проверка:**

```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"ivan\",\"password\":\"123456\",\"displayName\":\"Иван\"}'
```

Ответ с `accessToken` подтверждает, что бэкенд работает и подключён к базе.
