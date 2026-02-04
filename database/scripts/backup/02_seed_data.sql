-- =============================================
-- Тестовые данные
-- Выполняется ПОСЛЕ создания таблиц Hibernate
-- =============================================

-- Этот скрипт нужно запустить ВРУЧНУЮ после первого старта
-- или использовать DO блок с проверкой

DO $$
BEGIN
    -- Проверяем существует ли таблица users
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users') THEN
        
        -- Тестовый пользователь (пароль: 123456)
        INSERT INTO users (username, email, password, created_at, updated_at)
        VALUES (
            'demo',
            'demo@example.com',
            '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQnKL5n7M8j5WvJvjqnJbVxYz1Ey',
            NOW(),
            NOW()
        ) ON CONFLICT (email) DO NOTHING;
        
        RAISE NOTICE 'Demo user created or already exists';
        
    ELSE
        RAISE NOTICE 'Table users does not exist yet. Run this script after backend starts.';
    END IF;
END $$;