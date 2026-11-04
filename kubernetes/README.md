# Local Kubernetes Deployment

This directory translates `docker-compose.yml` to a local Kubernetes stack:

- `backend/` - the Spring Boot API, published through NodePort `30080`.
- `mysql/` - MySQL 8.4, published through NodePort `30306` with persistent storage.
- `rabbitmq/` - RabbitMQ with its management UI, published through NodePorts
  `30672` and `31672`, with persistent storage.

The backend image comes from GitHub Container Registry because Kubernetes pulls
container images from a registry; it does not use the image built on the local
machine by Docker Compose.

## Prerequisites

- A single-node local Kubernetes cluster, such as Docker Desktop Kubernetes,
  Minikube, or K3s.
- `kubectl` configured for that cluster.
- Internet access from the cluster nodes to pull MySQL, RabbitMQ, BusyBox, and
  the backend image.

`hostPath` persistent volumes are deliberately used for this local, single-node
exercise. They must be replaced with a real StorageClass before a multi-node or
production deployment.

## Publish The Backend Image

The `Publish Backend Container` GitHub Actions workflow publishes an image tag
for each configured branch. `main` publishes
`ghcr.io/sekionn/bar-scan-backend:latest`; this `Kubernetes-setup` branch
publishes `ghcr.io/sekionn/bar-scan-backend:kubernetes-setup`, which is the
image referenced by the backend Deployment.

Push this branch to GitHub and wait for the workflow to complete before
deploying. A Kubernetes Deployment pulls a published image tag, not Git source
code directly.

If the GHCR package remains private, create a GitHub personal access token with
the `read:packages` scope and add it to the cluster:

```powershell
kubectl apply -f kubernetes/namespace.yaml
kubectl -n bar-scan create secret docker-registry ghcr-read `
  --docker-server=ghcr.io `
  --docker-username=Sekionn `
  --docker-password=YOUR_READ_PACKAGES_TOKEN `
  --docker-email=support.bar-scan@juuls-trinkets.com
```

## Create Local Secrets

The Compose credentials are represented as Kubernetes Secrets but their real
files are ignored by Git. Copy each template once:

```powershell
Copy-Item kubernetes/backend/secret.yaml.example kubernetes/backend/secret.yaml
Copy-Item kubernetes/mysql/secret.yaml.example kubernetes/mysql/secret.yaml
Copy-Item kubernetes/rabbitmq/secret.yaml.example kubernetes/rabbitmq/secret.yaml
kubectl apply -f kubernetes/backend/secret.yaml
kubectl apply -f kubernetes/mysql/secret.yaml
kubectl apply -f kubernetes/rabbitmq/secret.yaml
```

Change the template values before using anything beyond local development. In
particular, use a long random JWT secret and non-default database credentials.

## Deploy

```powershell
kubectl apply -R -f kubernetes
kubectl -n bar-scan rollout status deployment/mysql
kubectl -n bar-scan rollout status deployment/rabbitmq
kubectl -n bar-scan rollout status deployment/bar-scan-backend
kubectl -n bar-scan get pods,services,persistentvolumeclaims
```

## Local Addresses

For Docker Desktop Kubernetes, these are normally available at:

- API: `http://localhost:30080`
- MySQL: `localhost:30306`
- RabbitMQ AMQP: `localhost:30672`
- RabbitMQ Management: `http://localhost:31672`

For Minikube or K3s, use the node IP instead of `localhost`. Check it with:

```powershell
kubectl get nodes -o wide
```

## Troubleshooting

```powershell
kubectl -n bar-scan get all
kubectl -n bar-scan logs deployment/bar-scan-backend --follow
kubectl -n bar-scan describe pod -l app.kubernetes.io/name=bar-scan-backend
```

KEDA is intentionally not included yet. When it is installed, a `ScaledObject`
can be added alongside the backend deployment.
