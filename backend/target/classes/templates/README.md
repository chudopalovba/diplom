# {{PROJECT_NAME}}

## Технологический стек

- **Backend:** {{BACKEND_LABEL}}
- **Frontend:** {{FRONTEND_LABEL}}
- **Database:** PostgreSQL
- **Docker:** {{USE_DOCKER_LABEL}}

## Структура проекта
```
{{PROJECT_NAME}}/
├── backend/ # Серверная часть
├── frontend/ # Клиентская часть
├── database/ # SQL скрипты
├── docker-compose.yml
├── .gitlab-ci.yml
└── README.md
```


## Быстрый старт

### С Docker

```
docker-compose up --build
```

Приложение будет доступно:

    Frontend: http://localhost:3000
    Backend API: http://localhost:{{BACKEND_PORT}}

Без Docker

См. README в каждой папке (backend/, frontend/).

## CI/CD

Проект настроен с GitLab CI/CD:

    build - сборка проекта
    sonar - анализ качества кода (SonarQube)
    push_artifacts - загрузка артефактов в Nexus
    deploy - деплой на сервер



### templates/docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: {{PROJECT_NAME_DASH}}-postgres
    environment:
      POSTGRES_DB: {{DB_NAME}}
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/scripts:/docker-entrypoint-initdb.d:ro
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: {{PROJECT_NAME_DASH}}-backend
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME={{DB_NAME}}
      - DB_USERNAME=postgres
      - DB_PASSWORD=postgres
    ports:
      - "{{BACKEND_PORT}}:{{BACKEND_PORT}}"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - app-network

  frontend:
    build: ./frontend
    container_name: {{PROJECT_NAME_DASH}}-frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - app-network

volumes:
  postgres_data:

networks:
  app-network:
    driver: bridge