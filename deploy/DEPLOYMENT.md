# Локальный деплой в Kubernetes (kind)

## Предварительная настройка

1. Включите Kubernetes в Docker Desktop:
   - Docker Desktop → Settings → Kubernetes → Enable Kubernetes → Apply & Restart.
2. Установите `kind` (https://kind.sigs.k8s.io/), если ещё не установлен.
3. Создайте кластер kind (1 узел):

```bash
kind create cluster --name sla-ping-monitor
```

> В дальнейшем используйте этот же `--name` в командах `make kind-load`.

## Подготовка секретов

```bash
cp deploy/k8s/dev/secrets/db.env.example deploy/k8s/dev/secrets/db.env
```

## Сборка образов и деплой

```bash
make -f deploy/Makefile build
make -f deploy/Makefile kind-load KIND_CLUSTER=sla-ping-monitor
make -f deploy/Makefile deploy
```

## Доступ к фронтенду

```bash
make -f deploy/Makefile front
```

Откройте: http://localhost:3000

## Примечания

- Внутри Pod нельзя обращаться к `http://localhost:8080` сервиса на хосте. Используйте
  `http://host.docker.internal:8080` и переменную `BACK_EXTERNAL_BASE_URL` (ConfigMap).
- NetworkPolicy для БД применяется только при наличии CNI, поддерживающего policy enforcement.
  В kind по умолчанию политики могут не применяться.
