-- =============================================
-- DevOps Platform - Тестовые данные
-- =============================================

-- Тестовый пользователь (пароль: 123456)
-- BCrypt hash для '123456'
INSERT INTO users (username, email, password, gitlab_user_id, gitlab_username)
VALUES (
    'testuser',
    'test@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQnKL5n7M8j5WvJvjqnJbVxYz1Ey',
    1,
    'testuser'
) ON CONFLICT (email) DO NOTHING;

-- Ещё один тестовый пользователь
INSERT INTO users (username, email, password)
VALUES (
    'admin',
    'admin@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQnKL5n7M8j5WvJvjqnJbVxYz1Ey'
) ON CONFLICT (email) DO NOTHING;

-- Тестовые проекты для testuser
INSERT INTO projects (name, description, status, backend_tech, frontend_tech, database_tech, use_docker, gitlab_url, git_clone_url, owner_id)
SELECT 
    'java-react-app',
    'Тестовый проект на Java + React с Docker',
    'DEPLOYED',
    'JAVA',
    'REACT',
    'POSTGRES',
    true,
    'http://gitlab.local/testuser/java-react-app',
    'git@gitlab.local:testuser/java-react-app.git',
    id
FROM users WHERE email = 'test@example.com'
ON CONFLICT DO NOTHING;

INSERT INTO projects (name, description, status, backend_tech, frontend_tech, database_tech, use_docker, gitlab_url, git_clone_url, owner_id)
SELECT 
    'python-vue-api',
    'REST API на Python Django с Vue.js фронтендом',
    'DEVELOPING',
    'PYTHON',
    'VUE',
    'POSTGRES',
    false,
    'http://gitlab.local/testuser/python-vue-api',
    'git@gitlab.local:testuser/python-vue-api.git',
    id
FROM users WHERE email = 'test@example.com'
ON CONFLICT DO NOTHING;

INSERT INTO projects (name, description, status, backend_tech, frontend_tech, database_tech, use_docker, owner_id)
SELECT 
    'csharp-angular-app',
    'Приложение на C# .NET с Angular',
    'CREATED',
    'CSHARP',
    'ANGULAR',
    'POSTGRES',
    true,
    id
FROM users WHERE email = 'test@example.com'
ON CONFLICT DO NOTHING;

-- Тестовые пайплайны
INSERT INTO pipelines (gitlab_pipeline_id, status, started_at, finished_at, deploy_url, project_id, trigger_type, duration_seconds)
SELECT 
    101,
    'SUCCESS',
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '1 hour 45 minutes',
    'http://java-react-app.apps.local',
    p.id,
    'manual',
    900
FROM projects p
JOIN users u ON p.owner_id = u.id
WHERE p.name = 'java-react-app' AND u.email = 'test@example.com'
ON CONFLICT DO NOTHING;

INSERT INTO pipelines (gitlab_pipeline_id, status, started_at, project_id, trigger_type)
SELECT 
    102,
    'RUNNING',
    NOW() - INTERVAL '10 minutes',
    p.id,
    'push'
FROM projects p
JOIN users u ON p.owner_id = u.id
WHERE p.name = 'python-vue-api' AND u.email = 'test@example.com'
ON CONFLICT DO NOTHING;

-- Тестовые этапы пайплайна
INSERT INTO pipeline_stages (name, status, pipeline_id, stage_order, started_at, finished_at)
SELECT 
    'build',
    'SUCCESS',
    pip.id,
    1,
    pip.started_at,
    pip.started_at + INTERVAL '5 minutes'
FROM pipelines pip
WHERE pip.gitlab_pipeline_id = 101
ON CONFLICT DO NOTHING;

INSERT INTO pipeline_stages (name, status, pipeline_id, stage_order, started_at, finished_at)
SELECT 
    'test',
    'SUCCESS',
    pip.id,
    2,
    pip.started_at + INTERVAL '5 minutes',
    pip.started_at + INTERVAL '10 minutes'
FROM pipelines pip
WHERE pip.gitlab_pipeline_id = 101
ON CONFLICT DO NOTHING;

INSERT INTO pipeline_stages (name, status, pipeline_id, stage_order, started_at, finished_at)
SELECT 
    'deploy',
    'SUCCESS',
    pip.id,
    3,
    pip.started_at + INTERVAL '10 minutes',
    pip.finished_at
FROM pipelines pip
WHERE pip.gitlab_pipeline_id = 101
ON CONFLICT DO NOTHING;

-- Этапы для running пайплайна
INSERT INTO pipeline_stages (name, status, pipeline_id, stage_order, started_at, finished_at)
SELECT 
    'build',
    'SUCCESS',
    pip.id,
    1,
    pip.started_at,
    pip.started_at + INTERVAL '3 minutes'
FROM pipelines pip
WHERE pip.gitlab_pipeline_id = 102
ON CONFLICT DO NOTHING;
INSERT INTO pipeline_stages (name, status, pipeline_id, stage_order, started_at)
SELECT 
    'test',
    'RUNNING',
    pip.id,
    2,
    pip.started_at + INTERVAL '3 minutes'
FROM pipelines pip
WHERE pip.gitlab_pipeline_id = 102
ON CONFLICT DO NOTHING;

INSERT INTO pipeline_stages (name, status, pipeline_id, stage_order)
SELECT 
    'deploy',
    'PENDING',
    pip.id,
    3
FROM pipelines pip
WHERE pip.gitlab_pipeline_id = 102
ON CONFLICT DO NOTHING;
