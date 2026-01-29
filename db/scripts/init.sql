-- =============================================
-- DevOps Platform - Полная инициализация БД
-- Этот файл можно использовать для Docker
-- =============================================

-- Выполнить все скрипты по порядку
\i /docker-entrypoint-initdb.d/02_create_tables.sql
\i /docker-entrypoint-initdb.d/03_create_functions.sql
\i /docker-entrypoint-initdb.d/04_seed_data.sql
\i /docker-entrypoint-initdb.d/05_create_views.sql