DROP TABLE IF EXISTS pipeline_stages CASCADE;
DROP TABLE IF EXISTS pipelines CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Удаление типов если существуют
DROP TYPE IF EXISTS project_status CASCADE;
DROP TYPE IF EXISTS pipeline_status CASCADE;
DROP TYPE IF EXISTS backend_tech CASCADE;
DROP TYPE IF EXISTS frontend_tech CASCADE;
DROP TYPE IF EXISTS database_tech CASCADE;

-- =============================================
-- Создание ENUM типов
-- =============================================

CREATE TYPE project_status AS ENUM (
    'CREATED',
    'DEVELOPING', 
    'DEPLOYED',
    'FAILED'
);

CREATE TYPE pipeline_status AS ENUM (
    'PENDING',
    'RUNNING',
    'SUCCESS',
    'FAILED',
    'CANCELED',
    'SKIPPED'
);

CREATE TYPE backend_tech AS ENUM (
    'JAVA',
    'CSHARP',
    'PYTHON'
);

CREATE TYPE frontend_tech AS ENUM (
    'REACT',
    'VUE',
    'ANGULAR'
);

CREATE TYPE database_tech AS ENUM (
    'POSTGRES'
);

-- =============================================
-- Таблица: users (Пользователи)
-- =============================================

CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50) NOT NULL UNIQUE,
    email               VARCHAR(100) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    gitlab_user_id      BIGINT,
    gitlab_username     VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_gitlab_user_id ON users(gitlab_user_id);

-- Комментарии
COMMENT ON TABLE users IS 'Таблица пользователей платформы';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.username IS 'Имя пользователя (уникальное)';
COMMENT ON COLUMN users.email IS 'Email пользователя (уникальный)';
COMMENT ON COLUMN users.password IS 'Хэш пароля (BCrypt)';
COMMENT ON COLUMN users.gitlab_user_id IS 'ID пользователя в GitLab';
COMMENT ON COLUMN users.gitlab_username IS 'Username в GitLab';

-- =============================================
-- Таблица: projects (Проекты)
-- =============================================

CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    status              project_status NOT NULL DEFAULT 'CREATED',
    
    -- Tech Stack (встроенные поля)
    backend_tech        backend_tech NOT NULL,
    frontend_tech       frontend_tech NOT NULL,
    database_tech       database_tech NOT NULL DEFAULT 'POSTGRES',
    use_docker          BOOLEAN NOT NULL DEFAULT true,
    
    -- GitLab интеграция
    gitlab_project_id   BIGINT,
    gitlab_url          VARCHAR(500),
    git_clone_url       VARCHAR(500),
    
    -- Deployment
    deploy_url          VARCHAR(500),
    
    -- Связи
    owner_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Timestamps
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничения
    CONSTRAINT uk_project_name_owner UNIQUE (name, owner_id)
);

-- Индексы для projects
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_gitlab_project_id ON projects(gitlab_project_id);
CREATE INDEX idx_projects_created_at ON projects(created_at DESC);
-- Комментарии
COMMENT ON TABLE projects IS 'Таблица проектов пользователей';
COMMENT ON COLUMN projects.name IS 'Название проекта (уникально для владельца)';
COMMENT ON COLUMN projects.status IS 'Статус проекта: CREATED, DEVELOPING, DEPLOYED, FAILED';
COMMENT ON COLUMN projects.backend_tech IS 'Backend технология: JAVA, CSHARP, PYTHON';
COMMENT ON COLUMN projects.frontend_tech IS 'Frontend технология: REACT, VUE, ANGULAR';
COMMENT ON COLUMN projects.use_docker IS 'Использовать Docker для деплоя';
COMMENT ON COLUMN projects.gitlab_project_id IS 'ID проекта в GitLab';
COMMENT ON COLUMN projects.deploy_url IS 'URL развёрнутого приложения';

-- =============================================
-- Таблица: pipelines (Пайплайны CI/CD)
-- =============================================

CREATE TABLE pipelines (
    id                      BIGSERIAL PRIMARY KEY,
    gitlab_pipeline_id      BIGINT,
    status                  pipeline_status NOT NULL DEFAULT 'PENDING',
    deploy_url              VARCHAR(500),
    started_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    finished_at             TIMESTAMP WITH TIME ZONE,
    
    -- Связи
    project_id              BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    
    -- Дополнительная информация
    trigger_type            VARCHAR(50) DEFAULT 'manual', -- manual, push, schedule
    commit_sha              VARCHAR(40),
    commit_message          TEXT,
    duration_seconds        INTEGER
);

-- Индексы для pipelines
CREATE INDEX idx_pipelines_project_id ON pipelines(project_id);
CREATE INDEX idx_pipelines_status ON pipelines(status);
CREATE INDEX idx_pipelines_started_at ON pipelines(started_at DESC);
CREATE INDEX idx_pipelines_gitlab_pipeline_id ON pipelines(gitlab_pipeline_id);

-- Комментарии
COMMENT ON TABLE pipelines IS 'Таблица пайплайнов CI/CD';
COMMENT ON COLUMN pipelines.gitlab_pipeline_id IS 'ID пайплайна в GitLab';
COMMENT ON COLUMN pipelines.status IS 'Статус: PENDING, RUNNING, SUCCESS, FAILED, CANCELED, SKIPPED';
COMMENT ON COLUMN pipelines.trigger_type IS 'Тип запуска: manual, push, schedule';

-- =============================================
-- Таблица: pipeline_stages (Этапы пайплайна)
-- =============================================

CREATE TABLE pipeline_stages (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    status          pipeline_status NOT NULL DEFAULT 'PENDING',
    started_at      TIMESTAMP WITH TIME ZONE,
    finished_at     TIMESTAMP WITH TIME ZONE,
    
    -- Связи
    pipeline_id     BIGINT NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    
    -- Порядок выполнения
    stage_order     INTEGER NOT NULL DEFAULT 0,
    
    -- Логи (опционально)
    log_url         VARCHAR(500),
    error_message   TEXT
);

-- Индексы для pipeline_stages
CREATE INDEX idx_pipeline_stages_pipeline_id ON pipeline_stages(pipeline_id);
CREATE INDEX idx_pipeline_stages_status ON pipeline_stages(status);

-- Комментарии
COMMENT ON TABLE pipeline_stages IS 'Этапы выполнения пайплайна';
COMMENT ON COLUMN pipeline_stages.name IS 'Название этапа: build, test, sonar, deploy';
COMMENT ON COLUMN pipeline_stages.stage_order IS 'Порядок выполнения этапа';

-- =============================================
-- Дополнительные таблицы (опционально)
-- =============================================

-- Таблица для хранения переменных окружения проекта
CREATE TABLE project_variables (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    key             VARCHAR(100) NOT NULL,
    value           TEXT NOT NULL,
    is_secret       BOOLEAN DEFAULT false,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_project_variable UNIQUE (project_id, key)
);

CREATE INDEX idx_project_variables_project_id ON project_variables(project_id);

COMMENT ON TABLE project_variables IS 'Переменные окружения проекта';
COMMENT ON COLUMN project_variables.is_secret IS 'Является ли переменная секретной';
-- Таблица для логов деплоя
CREATE TABLE deployment_logs (
    id              BIGSERIAL PRIMARY KEY,
    pipeline_id     BIGINT NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    log_level       VARCHAR(10) NOT NULL DEFAULT 'INFO', -- INFO, WARN, ERROR
    message         TEXT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deployment_logs_pipeline_id ON deployment_logs(pipeline_id);
CREATE INDEX idx_deployment_logs_created_at ON deployment_logs(created_at);

COMMENT ON TABLE deployment_logs IS 'Логи развёртывания';
