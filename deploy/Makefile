KIND_CLUSTER ?= kind

.PHONY: build kind-load deploy front

build:
	docker build -t back:dev ./apps/backend
	docker build -t front:dev ./apps/frontend-web

kind-load:
	kind load docker-image back:dev front:dev --name $(KIND_CLUSTER)

deploy:
	kubectl apply -k deploy/k8s/dev

front:
	kubectl -n app port-forward svc/front 3000:3000
