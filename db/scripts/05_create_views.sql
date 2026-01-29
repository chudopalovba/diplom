-- =============================================
-- DevOps Platform - Представления (Views)
-- =============================================

-- =============================================
-- View: Детальная информация о проектах
-- =============================================

CREATE OR REPLACE VIEW v_project_details AS
SELECT 
    p.id,
    p.name,
    p.description,
    p.status,
    p.backend_tech,
    p.frontend_tech,
    p.database_tech,
    p.use_docker,
    p.gitlab_url,
    p.git_clone_url,
    p.deploy_url,
    p.created_at,
    p.updated_at,
    u.id as owner_id,
    u.username as owner_username,
    u.email as owner_email,
    (SELECT COUNT(*) FROM pipelines pip WHERE pip.project_id = p.id) as total_pipelines,
    (SELECT COUNT(*) FROM pipelines pip WHERE pip.project_id = p.id AND pip.status = 'SUCCESS') as successful_pipelines
FROM projects p
JOIN users u ON p.owner_id = u.id;

COMMENT ON VIEW v_project_details IS 'Детальная информация о проектах с данными владельца';

-- =============================================
-- View: Статистика пользователей
-- =============================================

CREATE OR REPLACE VIEW v_user_stats AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.created_at,
    COUNT(DISTINCT p.id) as total_projects,
    COUNT(DISTINCT CASE WHEN p.status = 'DEPLOYED' THEN p.id END) as deployed_projects,
    COUNT(DISTINCT CASE WHEN p.status = 'DEVELOPING' THEN p.id END) as developing_projects,
    COUNT(DISTINCT CASE WHEN p.status = 'FAILED' THEN p.id END) as failed_projects,
    COUNT(DISTINCT pip.id) as total_pipelines,
    MAX(pip.started_at) as last_pipeline_at
FROM users u
LEFT JOIN projects p ON u.id = p.owner_id
LEFT JOIN pipelines pip ON p.id = pip.project_id
GROUP BY u.id, u.username, u.email, u.created_at;

COMMENT ON VIEW v_user_stats IS 'Статистика по пользователям';

-- =============================================
-- View: Последние пайплайны
-- =============================================

CREATE OR REPLACE VIEW v_recent_pipelines AS
SELECT 
    pip.id,
    pip.status,
    pip.started_at,
    pip.finished_at,
    pip.duration_seconds,
    pip.trigger_type,
    pip.deploy_url,
    p.id as project_id,
    p.name as project_name,
    u.id as user_id,
    u.username
FROM pipelines pip
JOIN projects p ON pip.project_id = p.id
JOIN users u ON p.owner_id = u.id
ORDER BY pip.started_at DESC;

COMMENT ON VIEW v_recent_pipelines IS 'Последние пайплайны со всеми связями';

-- =============================================
-- View: Статистика по технологиям
-- =============================================

CREATE OR REPLACE VIEW v_tech_stats AS
SELECT 
    backend_tech,
    frontend_tech,
    use_docker,
    COUNT(*) as project_count,
    COUNT(*) FILTER (WHERE status = 'DEPLOYED') as deployed_count
FROM projects
GROUP BY backend_tech, frontend_tech, use_docker
ORDER BY project_count DESC;

COMMENT ON VIEW v_tech_stats IS 'Статистика использования технологий';