# 📦 Repo Overview: `akt-reactive-springboot-cloud-kubernetes-microservices-with-oauth`

**Author**: [kiduknott](https://github.com/kiduknott)  
**Repo**: [GitHub Link](https://github.com/kiduknott/akt-reactive-springboot-cloud-kubernetes-microservices-with-oauth)  
**Tech Stack**:
- Spring Boot (Reactive)
- OAuth 2.0 + OIDC
- Docker + Kubernetes
- Microservices architecture
- Gradle multi-project setup
- RabbitMQ + Kafka (via Docker Compose)

---

## 📁 Key Folders

| Folder | Description |
|--------|-------------|
| `api/` | API gateway and routing logic |
| `microservices/` | Core services (e.g., product, review, recommendation) |
| `spring-cloud/` | Config server, discovery, gateway |
| `keystore/` | TLS and OAuth/OIDC setup |
| `util/` | OpenAPI specs and shared utilities |

---

## 🔐 Security Highlights

- OAuth 2.0 and OIDC integration
- HTTPS enforcement via keystore
- API gateway secured with token validation
- Conditional access simulated via scopes and roles

---

## 🧪 Testing & Deployment

- Unit and component tests
- Bash scripts for microservice testing
- Docker Compose files for RabbitMQ and Kafka
- Kubernetes-ready structure (manifests not visible in root)

---

## ✅ Automated Testing

This project includes component and unit tests that run automatically with each build.

### 🧪 To Run Tests Manually

```bash
./gradlew test
````

Or, for a specific module:

```bash
./gradlew :<module-name>:test
````

🛠️ Test Coverage
- Unit tests for core business logic
- Component tests for service interactions
- Reactive flow validation (WebFlux)
- OAuth token validation scenarios
  Tests are triggered during build and CI workflows to ensure reliability across microservices.

---

## 🧪 Local Microservices Sanity Tests

This project includes a test harness for running all microservices locally using Docker. The test script spins up the full stack via the `product-composite` gateway.

### ✅ Prerequisites

- Docker installed and running locally before executing the script
- Git installed
- Bash shell (Linux/macOS or WSL on Windows)
- Logs and service health can be monitored via Docker Dashboard or docker ps
- You may need to grant execute permissions to the script
```bash
chmod +x ./test-all-microservices-on-docker-via-product-composite-02.bash
```

### 📦 Clone the Repository

```bash
git clone https://github.com/kiduknott/akt-reactive-springboot-cloud-kubernetes-microservices-with-oauth.git
cd akt-reactive-springboot-cloud-kubernetes-microservices-with-oauth
```

### 🚀 Start All Microservices And Dependent Services And Run The Sanity Tests

```bash
./test-all-microservices-on-docker-via-product-composite-02.bash start
```

This will:
- Build and start all microservices using Docker
- Route traffic through the  gateway
- Initialise supporting services (e.g., RabbitMQ, Kafka)

### 🛑 Stop All Microservices

```bash
./test-all-microservices-on-docker-via-product-composite-02.bash stop
```

This will:
- Gracefully shut down all containers
- Clean up the test environment

---
