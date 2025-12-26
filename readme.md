# SLA Ping Monitor Backend

Backend-сервис для **SLA Ping Monitor** (MVP). Выполняет плановые проверки HTTP endpoints, хранит результаты и
отдаёт REST API для UI (дашборды/алертинг в UI).

## Возможности (MVP)

- CRUD для endpoints.
- Хранение результатов проверок.
- Статистика по окнам (p50/p95/p99, error rate) по запросу.
- Хранение правил алертов и событий (решения принимает UI).
- Discovery backend-нод через heartbeat.

## Стек

- Kotlin 2.0
- Spring Boot 3.5
- PostgreSQL
- Gradle Kotlin DSL

## Быстрый старт

### Требования

- Java 21+
- PostgreSQL 14+

### Конфигурация

Переменные окружения (по умолчанию):

- `DB_URL` — `jdbc:postgresql://localhost:5432/sla_ping_monitor`
- `DB_USER` — `postgres`
- `DB_PASSWORD` — `postgres`

Дополнительные настройки см. в `apps/backend/src/main/resources/application.yml`.

### Запуск

```bash
./gradlew bootRun
```

Приложение будет доступно на `http://localhost:8080`.

### OpenAPI

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Описание репозитория

Этот репозиторий содержит backend-часть проекта **SLA Ping Monitor**:

- принимает и хранит конфигурацию endpoints;
- выполняет проверки (логика исполнения и планировщика — следующая итерация);
- отдаёт API для UI;
- хранит правила алертов и события (решения принимает UI).

## Структура

- `apps/backend/src/main/kotlin/com/acme/slamonitor` — код приложения
- `apps/backend/src/main/resources/application.yml` — конфигурация

## Полезные эндпоинты

- `POST /api/endpoints`
- `GET /api/endpoints`
- `GET /api/endpoints/{id}`
- `PUT /api/endpoints/{id}`
- `DELETE /api/endpoints/{id}`
- `GET /api/endpoints/{id}/stats?windowSec=900`
- `GET /api/endpoints/{id}/checks?from=...&to=...`
- `GET /api/endpoints/summary?windowSec=900`
- `POST /api/alert-rules`
- `GET /api/alert-rules?endpointId=...`
- `PUT /api/alert-rules/{id}`
- `DELETE /api/alert-rules/{id}`
- `POST /api/alert-events`
- `GET /api/alert-events?state=OPEN`
- `POST /api/nodes/heartbeat`
- `GET /api/nodes`
- `GET /api/nodes/{nodeId}/metrics`
