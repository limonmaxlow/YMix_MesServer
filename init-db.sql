-- Выполнить один раз после старта SQL Server (Hibernate создаст таблицы сам через ddl-auto=update,
-- но саму базу данных СУБД не создаёт автоматически — её нужно создать заранее).

IF DB_ID('messenger') IS NULL
BEGIN
    CREATE DATABASE messenger;
END
GO

-- Отдельный логин для приложения (не используем sa в рабочем режиме)
USE master;
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'messenger_app')
BEGIN
    CREATE LOGIN messenger_app WITH PASSWORD = 'StrongPass123!';
END
GO

USE messenger;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'messenger_app')
BEGIN
    CREATE USER messenger_app FOR LOGIN messenger_app;
    ALTER ROLE db_owner ADD MEMBER messenger_app; -- на старте проще всего; потом можно сузить права
END
GO
