-- =============================================
-- Тестовые данные (запускать ПОСЛЕ первого старта бэкенда)
-- docker-compose exec postgres psql -U postgres -d devops_platform -f /docker-entrypoint-initdb.d/99_seed_data.sql
-- =============================================

-- Тестовый пользователь: demo@example.com / 123456
INSERT INTO users (username, email, password, created_at, updated_at)
VALUES (
    'demo',
    'demo@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQnKL5n7M8j5WvJvjqnJbVxYz1Ey',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Тестовый проект
INSERT INTO projects (
    name, description, status, 
    backend_tech, frontend_tech, database_tech, use_docker,
    gitlab_url, git_clone_url, 
    owner_id, created_at, updated_at
)
SELECT 
    'demo-project',
    'Демонстрационный проект',
    'CREATED',
    'JAVA', 'REACT', 'POSTGRES', true,
    'http://gitlab.local/demo/demo-project',
    'git@gitlab.local:demo/demo-project.git',
    id, NOW(), NOW()
FROM users WHERE email = 'demo@example.com'
ON CONFLICT DO NOTHING;

SELECT 'Seed data inserted!' as status;
