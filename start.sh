#!/bin/bash

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}"
echo "╔════════════════════════════════════════════╗"
echo "║     🚀 DevOps Platform - Starting...       ║"
echo "╚════════════════════════════════════════════╝"
echo -e "${NC}"

# Проверка Docker
if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker не запущен!${NC}"
    exit 1
fi

# Останавливаем старые контейнеры
echo -e "${YELLOW}🛑 Останавливаем старые контейнеры...${NC}"
docker-compose down 2>/dev/null || true

# Собираем и запускаем
echo -e "${YELLOW}🔨 Сборка и запуск...${NC}"
docker-compose up --build -d

# Ждём готовности
echo -e "${YELLOW}⏳ Ожидание готовности сервисов...${NC}"

echo -n "PostgreSQL: "
until docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}✅${NC}"

echo -n "Backend: "
for i in {1..30}; do
    if curl -sf http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo -e " ${GREEN}✅${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo -n "Frontend: "
for i in {1..15}; do
    if curl -sf http://localhost:3000 > /dev/null 2>&1; then
        echo -e " ${GREEN}✅${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo ""
echo -e "${GREEN}"
echo "╔════════════════════════════════════════════╗"
echo "║       🎉 Платформа запущена!               ║"
echo "╠════════════════════════════════════════════╣"
echo "║                                            ║"
echo "║  🌐 Frontend:  http://localhost:3000       ║"
echo "║  🔧 Backend:   http://localhost:8080/api   ║"
echo "║  🐘 PostgreSQL: localhost:5432             ║"
echo "║                                            ║"
echo "╚════════════════════════════════════════════╝"
echo -e "${NC}"