#!/bin/bash

set -e

echo "🚀 Настройка базы данных DevOps Platform..."

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Конфигурация
CONTAINER_NAME="devops-postgres"
DB_NAME="devops_platform"
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_PORT="5432"

# Проверка Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker не установлен!${NC}"
    exit 1
fi

# Остановка старого контейнера если есть
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${YELLOW}⏹️  Останавливаем старый контейнер...${NC}"
    docker stop $CONTAINER_NAME 2>/dev/null ⠺⠺⠞⠞⠞⠺⠞⠵⠵⠟⠟⠞⠟⠵⠵⠞⠵⠟⠟⠞⠵⠺⠺⠟⠵⠺⠺⠵⠞⠺⠟⠞⠵⠟⠵⠞⠞⠞⠵⠟⠞⠞⠵⠺⠺⠺⠵⠟ true
fi

# Запуск PostgreSQL
echo -e "${YELLOW}🐘 Запускаем PostgreSQL...${NC}"
docker run -d \
    --name $CONTAINER_NAME \
    -e POSTGRES_DB=$DB_NAME \
    -e POSTGRES_USER=$DB_USER \
    -e POSTGRES_PASSWORD=$DB_PASSWORD \
    -p $DB_PORT:5432 \
    -v devops_postgres_data:/var/lib/postgresql/data \
    postgres:15-alpine

# Ожидание готовности
echo -e "${YELLOW}⏳ Ожидаем готовности PostgreSQL...${NC}"
until docker exec $CONTAINER_NAME pg_isready -U $DB_USER -d $DB_NAME > /dev/null 2>&1; do
    sleep 1
    echo -n "."
done
echo ""

# Выполнение скриптов
echo -e "${YELLOW}📜 Выполняем SQL скрипты...${NC}"

SCRIPTS_DIR="$(dirname "$0")/scripts"

if [ -f "$SCRIPTS_DIR/02_create_tables.sql" ]; then
    echo "  → Создание таблиц..."
    docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SCRIPTS_DIR/02_create_tables.sql"
fi

if [ -f "$SCRIPTS_DIR/03_create_functions.sql" ]; then
    echo "  → Создание функций..."
    docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SCRIPTS_DIR/03_create_functions.sql"
fi

if [ -f "$SCRIPTS_DIR/04_seed_data.sql" ]; then
    echo "  → Загрузка тестовых данных..."
    docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SCRIPTS_DIR/04_seed_data.sql"
fi

if [ -f "$SCRIPTS_DIR/05_create_views.sql" ]; then
    echo "  → Создание представлений..."
    docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SCRIPTS_DIR/05_create_views.sql"
fi

# Проверка
echo -e "${YELLOW}🔍 Проверяем таблицы...${NC}"
docker exec $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME -c "\dt"

echo ""
echo -e "${GREEN}✅ База данных успешно настроена!${NC}"
echo ""
echo -e "📊 Подключение:"
echo -e "   Host: localhost"
echo -e "   Port: $DB_PORT"
echo -e "   Database: $DB_NAME"
echo -e "   User: $DB_USER"
echo -e "   Password: $DB_PASSWORD"
echo ""
echo -e "🔗 JDBC URL: jdbc:postgresql://localhost:$DB_PORT/$DB_NAME"
echo ""
echo -e "📝 Тестовый пользователь:"
echo -e "   Email: test@example.com"
echo -e "   Password: 123456"