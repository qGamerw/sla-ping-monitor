# Деплой в Docker Desktop Kubernetes (локально)

Эти манифесты и Dockerfile предназначены для локального деплоя в Docker Desktop Kubernetes
без публикации образов в registry.

## Сборка образов

Из корня репозитория:

```bash
docker build -f deploy/backend/Dockerfile -t sla-ping-monitor-backend:local .
docker build -f deploy/frontend/Dockerfile \
  --build-arg NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 \
  -t sla-ping-monitor-frontend:local .
```

## Скрипт сборки и деплоя

```bash
./deploy/deploy.sh
```

Можно переопределить базовый URL для API:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 ./deploy/deploy.sh
```

Скрипт заранее подтягивает образ базы данных. При необходимости можно переопределить его:

```bash
POSTGRES_IMAGE=postgres:16 ./deploy/deploy.sh
```

## Деплой

```bash
kubectl apply -k deploy/k8s
```

Бэкенд-под использует host networking для доступа к сервисам на вашей машине.
В URL эндпоинтов используйте `http://localhost:<port>` или `http://host.docker.internal:<port>`
в зависимости от способа публикации сервиса.

## Доступ к сервисам

* Frontend: http://localhost:3000
* Backend: http://localhost:8080

## Очистка

```bash
kubectl delete -k deploy/k8s
```
