-- =============================================
-- DevOps Platform - Права доступа
-- =============================================

-- Создание роли для приложения (если нужна отдельная)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'devops_app') THEN
        CREATE ROLE devops_app WITH LOGIN PASSWORD 'devops_app_password';
    END IF;
END
$$;

-- Права на схему
GRANT USAGE ON SCHEMA public TO devops_app;

-- Права на таблицы
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO devops_app;

-- Права на последовательности (для SERIAL/BIGSERIAL)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO devops_app;

-- Права для будущих объектов
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO devops_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO devops_app;