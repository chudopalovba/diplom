-- =============================================
-- Функции и триггеры (создаются после таблиц)
-- Этот файл можно запустить вручную после первого старта
-- =============================================

-- Функция для автообновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры создадим позже вручную
-- (таблиц ещё нет при первом запуске)