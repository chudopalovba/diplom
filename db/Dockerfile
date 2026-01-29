FROM postgres:15-alpine

# Метаданные
LABEL maintainer="DevOps Platform"
LABEL version="1.0"

# Копируем скрипты инициализации
COPY scripts/02_create_tables.sql /docker-entrypoint-initdb.d/01_create_tables.sql
COPY scripts/03_create_functions.sql /docker-entrypoint-initdb.d/02_create_functions.sql
COPY scripts/04_seed_data.sql /docker-entrypoint-initdb.d/03_seed_data.sql
COPY scripts/05_create_views.sql /docker-entrypoint-initdb.d/04_create_views.sql

# Настройки PostgreSQL для разработки
RUN echo "log_statement = 'all'" >> /usr/local/share/postgresql/postgresql.conf.sample

# Порт
EXPOSE 5432

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
    CMD pg_isready -U postgres -d devops_platform || exit 1