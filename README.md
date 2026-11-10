# web-java-gradle-template

## Description
A secured api/frontend for the AQL.

---

## To run

### Start services
```bash
docker compose up --build
```

### View frontend
[localhost](https://localhost)

### Health check
```bash
curl -ikL localhost/health
```

### Call API
```bash
curl -ikL localhost/api/data
```

### Run playwrite tests
```bash
docker compose --profile test up
```

### Stop services
```bash
docker compose down
```

### Cleanup docker
```bash
docker volume prune && docker system prune
```
---

## Services

### Caddy
Reverse proxy with additional rate limit plugin. Manages self-signed ssl certificates.

### FastAPI
A Java Spring REST api only gateway protected by docker secrets file. 

### Frontend
A Java / React simple page that displays api data.

### Node
A Java Spring BFF (backend for frontend) gateway protected by docker secrets file.

### Playwrite
Tests browser and api.

---

## Creating docker secrets
```bash
openssl rand -hex 32
```

---

## How docker secrets are shared
```text
             Docker secret
                  │
          ┌───────┴────────┐
          ▼                ▼
       Node.js           FastAPI
          │                │
          └── same key ────┘
```

---
## Network Flow

```text
                  PUBLIC
        ┌───────────┼───────────┐
        │           │           │
      Caddy      frontend     MinIO
        │                       │
        │                       │
        └───────────────────────┘

                  BACKEND
        ┌───────────┼───────────┐
        │           │           │
       Node      FastAPI       MinIO
```
---

## System Flow

```mermaid
flowchart TB
    %% Application Architecture
    node_0["Browser"]
    node_1["Frontend"]
    node_0 --> node_1
    node_2["Caddy"]
    node_1 --> node_2
    node_3["Node"]
    node_2 --> node_3
    node_4["FastAPI"]
    node_3 --> node_4
```