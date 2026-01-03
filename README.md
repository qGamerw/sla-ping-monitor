# SLA Ping Monitor

Сервис для мониторинга HTTP-эндпоинтов с хранением результатов и агрегированной статистикой (p50/p95/p99).

## Быстрый старт (backend + PostgreSQL)

### Вариант 1. Docker Compose

```bash
# Из корня репозитория
cd deploy/compose

# Соберите локальный образ backend
docker build -t sla-ping-monitor-backend:local ../../back

# Поднимите стек
docker compose up -d
```

Backend будет доступен на `http://localhost:8080`.

### Вариант 2. PostgreSQL отдельно + запуск backend локально

```bash
# Запустить PostgreSQL отдельно
docker run --name sla-ping-monitor-db \
  -e POSTGRES_DB=sla_ping_monitor \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:14

# Запуск backend (требуется установленный Gradle и JDK 21)
cd back
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sla_ping_monitor \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
  gradle bootRun
```

## Переменные окружения

Backend использует стандартные настройки Spring Boot:

- `SPRING_DATASOURCE_URL` — строка подключения к PostgreSQL.
- `SPRING_DATASOURCE_USERNAME` — пользователь БД.
- `SPRING_DATASOURCE_PASSWORD` — пароль БД.
- `SPRING_JPA_HIBERNATE_DDL_AUTO` — режим миграций (`create-drop`, `update`, и т.д.).
- `SERVER_PORT` — порт HTTP-сервера (по умолчанию `8080`).

## Примеры конфигов

### Пример `.env` для docker-compose

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sla_ping_monitor
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SERVER_PORT=8080
```

### Пример `application.yml` override

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sla_ping_monitor
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
server:
  port: 8080
```

## Примеры curl для основных API

### Создать эндпоинт

```bash
curl -X POST http://localhost:8080/api/endpoints \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Home page",
    "url": "https://example.com",
    "method": "GET",
    "timeoutMs": 1500,
    "intervalSec": 60,
    "expectedStatus": [200, 301, 302],
    "enabled": true,
    "tags": ["prod", "public"]
  }'
```

### Получить список эндпоинтов

```bash
curl http://localhost:8080/api/endpoints
```

### Получить конкретный эндпоинт

```bash
curl http://localhost:8080/api/endpoints/<endpointId>
```

### Статистика по эндпоинту

```bash
curl "http://localhost:8080/api/endpoints/<endpointId>/stats?windowSec=3600"
```

### Проверки по эндпоинту за период

```bash
curl "http://localhost:8080/api/endpoints/<endpointId>/checks?from=2024-01-01T00:00:00Z&to=2024-01-02T00:00:00Z"
```

### Краткое резюме по всем эндпоинтам

```bash
curl "http://localhost:8080/api/endpoints/summary?windowSec=3600"
```

### Heartbeat backend-ноды

```bash
curl -X POST http://localhost:8080/api/nodes/heartbeat \
  -H 'Content-Type: application/json' \
  -d '{
    "nodeId": "backend-1",
    "baseUrl": "http://localhost:8080",
    "startedAt": "2024-01-01T00:00:00Z",
    "meta": {"region": "eu-central"}
  }'
```

### Список backend-нод

```bash
curl http://localhost:8080/api/nodes
```

## Краткое руководство

### Как добавить endpoint для мониторинга

1. Отправьте `POST /api/endpoints` с минимальными полями: `name` и `url`.
2. При необходимости задайте `timeoutMs`, `intervalSec`, `expectedStatus`, `headers`, `tags`.
3. Используйте `GET /api/endpoints` для проверки, что эндпоинт добавлен.

### Как трактовать p95/p99

- `p95` — значение задержки, ниже которого лежит 95% измерений в окне.
- `p99` — значение задержки, ниже которого лежит 99% измерений в окне.
- Если `insufficientSamples=true`, значит для расчета перцентилей недостаточно данных.
- `errorRate` показывает долю неуспешных проверок в выбранном окне.

## Документация API

OpenAPI-описание находится в `contracts/openapi/api.yaml`.

## Локальный деплой в Kubernetes (kind)

Инструкция: [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md).
