-- =============================================
-- DevOps Platform - Функции и триггеры
-- =============================================

-- =============================================
-- Функция: Автообновление updated_at
-- =============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Триггеры для автообновления updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Функция: Подсчёт проектов пользователя
-- =============================================

CREATE OR REPLACE FUNCTION get_user_project_stats(p_user_id BIGINT)
RETURNS TABLE (
    total_projects BIGINT,
    deployed_projects BIGINT,
    developing_projects BIGINT,
    failed_projects BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_projects,
        COUNT(*) FILTER (WHERE status = 'DEPLOYED')::BIGINT as deployed_projects,
        COUNT(*) FILTER (WHERE status = 'DEVELOPING')::BIGINT as developing_projects,
        COUNT(*) FILTER (WHERE status = 'FAILED')::BIGINT as failed_projects
    FROM projects
    WHERE owner_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- Функция: Получение последнего пайплайна проекта
-- =============================================

CREATE OR REPLACE FUNCTION get_latest_pipeline(p_project_id BIGINT)
RETURNS TABLE (
    pipeline_id BIGINT,
    status pipeline_status,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    deploy_url VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.status,
        p.started_at,
        p.finished_at,
        p.deploy_url
    FROM pipelines p
    WHERE p.project_id = p_project_id
    ORDER BY p.started_at DESC
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- Функция: Обновление статуса проекта при завершении пайплайна
-- =============================================

CREATE OR REPLACE FUNCTION update_project_status_on_pipeline_complete()
RETURNS TRIGGER AS $$
BEGIN
    -- Если пайплайн успешно завершён и имеет deploy_url
    IF NEW.status = 'SUCCESS' AND NEW.deploy_url IS NOT NULL THEN
        UPDATE projects 
        SET status = 'DEPLOYED', 
            deploy_url = NEW.deploy_url,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.project_id;
    -- Если пайплайн провалился
    ELSIF NEW.status = 'FAILED' THEN
        UPDATE projects 
        SET status = 'FAILED',
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.project_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_project_on_pipeline_complete
    AFTER UPDATE OF status ON pipelines
    FOR EACH ROW
    WHEN (NEW.status IN ('SUCCESS', 'FAILED') AND OLD.status != NEW.status)
    EXECUTE FUNCTION update_project_status_on_pipeline_complete();

-- =============================================
-- Функция: Расчёт длительности пайплайна
-- =============================================

CREATE OR REPLACE FUNCTION calculate_pipeline_duration()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.finished_at IS NOT NULL AND NEW.started_at IS NOT NULL THEN
        NEW.duration_seconds = EXTRACT(EPOCH FROM (NEW.finished_at - NEW.started_at))::INTEGER;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_pipeline_duration
    BEFORE UPDATE ON pipelines
    FOR EACH ROW
    WHEN (NEW.finished_at IS NOT NULL)
    EXECUTE FUNCTION calculate_pipeline_duration();