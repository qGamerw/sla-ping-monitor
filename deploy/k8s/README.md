# Kubernetes (k3s single-node) deployment

> Манифесты ориентированы на k3s с одним узлом и встроенным ingress-контроллером Traefik.

## Быстрый старт (dev overlay)

1. Соберите локальные образы и импортируйте их в k3s:

```bash
# Backend
cd back
docker build -t sla-ping-monitor-backend:local .

# Frontend
cd ../front
docker build -t sla-ping-monitor-frontend:local .

# Импорт в k3s (если используете containerd)
# k3s ctr images import <tar>
```

2. Убедитесь, что kubeconfig указывает на ваш k3s кластер.

3. Примените манифесты:

```bash
kubectl apply -k deploy/k8s/overlays/dev
```

4. Проверьте статус:

```bash
kubectl -n sla-ping-monitor get pods
kubectl -n sla-ping-monitor get svc
kubectl -n sla-ping-monitor get ingress
```

По умолчанию frontend доступен через Ingress Traefik. Для доступа с локального хоста можно
использовать NodeIP или настроить port-forward:

```bash
kubectl -n sla-ping-monitor port-forward svc/frontend 3000:3000
kubectl -n sla-ping-monitor port-forward svc/backend 8080:8080
```

## Переменные и секреты

- `sla-backend-config` — настройки подключения backend к PostgreSQL.
- `sla-backend-secret` — пароль к БД.
- `sla-postgres-secret` — пароль PostgreSQL.
- `sla-frontend-config` — базовый URL backend для frontend.

## Примечания

- В `overlays/prod` подключен HPA (требуется metrics-server).
- Для продакшена укажите реальные образы в `deploy/k8s/overlays/prod/patch-images.yaml`.
