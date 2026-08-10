# ktor-notes

A notes REST API built with [Ktor](https://ktor.io), following a layered
architecture (domain → application → data → api). Writes go through an
async, at-least-once, idempotent command pipeline backed by RabbitMQ, with
optimistic concurrency control on updates and a Postgres/Exposed persistence
layer.

## Features

- **REST API** for creating, reading, updating, and deleting notes
- **Async write path** — `POST`/`PATCH` publish a command to RabbitMQ and
  return `202 Accepted` immediately; a background worker applies the change
- **Idempotent creates** — each create command carries a `commandId`;
  duplicate deliveries are detected and skipped via a `processed_commands`
  table
- **Optimistic concurrency** — updates require the client's last-known
  `version`; a stale version is rejected with a conflict
- **Retry + dead-letter queues** — transient failures (e.g. a dropped DB
  connection) are retried with a delay via a RabbitMQ TTL/DLX topology;
  exhausted or non-retryable failures land in a DLQ instead of being lost
- **Postgres** persistence via [Exposed](https://github.com/JetBrains/Exposed),
  with schema migrations managed by [Flyway](https://flywaydb.org/)
- **Dependency injection** via [Koin](https://insert-koin.io/)
- Health check, CORS, structured JSON error responses, and call logging

## Architecture

```
api/          HTTP routes, request/response DTOs, DTO ↔ domain mappers
application/  Use-case orchestration (NoteService), commands, messages
domain/       Core model, repository interface, sealed Answer/AnswerError result type
data/         Postgres (Exposed) persistence, RabbitMQ publisher/workers/topology
di/           Koin module wiring
plugins/      Ktor application setup (routing, security, db, workers, ...)
```

- `domain` has no framework dependencies — it defines the `Note` model,
  the `NoteRepository` port, and a `Answer<T>` / `AnswerError` result type
  used instead of exceptions at the repository boundary.
- `application` (`NoteService`) implements the use cases and translates
  `AnswerError` into typed domain exceptions for the synchronous read path.
- `data` provides the Postgres implementation of the repository plus the
  RabbitMQ command publisher and the two background workers that consume
  create/update commands.
- `api` exposes everything over HTTP.

### Write flow

```
Client → POST /notes → NoteService.submitCreate → publish CreateNoteMessage
                                                          │
                                                          ▼
                                          RabbitMQ (notes.commands exchange)
                                                          │
                                                          ▼
                                                  CreateNoteWorker
                                          (idempotent insert + ack/retry/DLQ)
```

`PATCH /notes/{id}` follows the same pattern via `UpdateNoteMessage` /
`UpdateNoteWorker`, enforcing `expectedVersion` optimistic locking.

## Quickstart

The fastest way to run everything (Postgres, RabbitMQ, the API, and an
Nginx reverse proxy) is Docker Compose — no local JDK or Gradle required:

```bash
docker compose up --build
```

Once the stack is healthy, the API is available at `http://localhost:8080`:

```bash
curl http://localhost:8080/notes
```

RabbitMQ's management UI is at `http://localhost:15672` (`guest` / `guest`).

Stop everything with:

```bash
docker compose down
```

Add `-v` to also drop the Postgres volume (`docker compose down -v`).

### Running locally (without Docker)

Requires JDK 21+. Start Postgres and RabbitMQ yourself (or reuse the ones
from `docker compose up postgres rabbitmq`), then export the environment
variables listed below and run:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/notes
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest

./gradlew run
```

The server starts on `http://0.0.0.0:8080`. Database schema is created
automatically on boot via Flyway migrations (`src/main/resources/db/migration`).

### Trying it out

`requests.http` at the repo root has ready-to-run sample requests
(usable directly from IntelliJ/Ktor's HTTP client, or adapt them to `curl`).

## Configuration

All configuration is environment-driven (see `src/main/resources/application.yaml`):

| Variable             | Description                          |
|-----------------------|--------------------------------------|
| `DATABASE_URL`         | JDBC URL for Postgres                |
| `DATABASE_USER`        | Postgres user                        |
| `DATABASE_PASSWORD`    | Postgres password                    |
| `RABBITMQ_HOST`        | RabbitMQ host                        |
| `RABBITMQ_PORT`        | RabbitMQ port                        |
| `RABBITMQ_USERNAME`    | RabbitMQ user                        |
| `RABBITMQ_PASSWORD`    | RabbitMQ password                    |

`docker-compose.yml` already provides sane defaults for all of these when
running via `docker compose up`.

## API reference

Base path: `/notes`. All bodies are JSON.

| Method  | Path          | Body                                   | Response                   | Notes                                    |
|---------|---------------|-----------------------------------------|-----------------------------|-------------------------------------------|
| `GET`   | `/notes`      | —                                        | `200` — `NoteResponse[]`    | Synchronous read                          |
| `GET`   | `/notes/{id}` | —                                        | `200` — `NoteResponse`, `404` if missing | Synchronous read           |
| `POST`  | `/notes`      | `{ "title": string, "content": string }` | `202` — `{ "commandId": string }` | Async — published to RabbitMQ    |
| `PATCH` | `/notes/{id}` | `{ "version": long, "title"?: string, "content"?: string }` | `202` — `{ "commandId": string }` | Async, optimistic concurrency via `version` |
| `DELETE`| `/notes/{id}` | —                                        | `204`, `404` if missing     | Synchronous                               |

`NoteResponse`:

```json
{ "id": 1, "title": "string", "content": "string", "version": 0 }
```

Errors are returned as `{ "error": "message" }` with an appropriate HTTP
status (`400` invalid input, `404` not found, `409` version conflict,
`500` unexpected).

> **Note:** JWT authentication (`server-auth-jwt`) is configured but not yet
> wired into the routes below — endpoints are currently open. Don't rely on
> the JWT setup for access control until routes are wrapped with
> `authenticate { ... }`.

## Development

```bash
./gradlew test    # run tests
./gradlew build   # build (compiles + tests + packages)
./gradlew run     # run the server
```

Tests cover the API layer (`NoteRoutesTest`), the application/use-case
layer (`NoteServiceTest`), and the Postgres data layer
(`PostgresNoteDbSourceTest`), using fakes/in-memory doubles for the layers
above the one under test (see `src/test/kotlin/testUtil`).

## Project structure

```
src/main/kotlin/
├── api/            routes, dto, mappers
├── application/    use cases, commands, messages
├── data/           Postgres + RabbitMQ implementations
├── di/             Koin module
├── domain/         model, repository port, result types, exceptions
└── plugins/        Ktor application module wiring
src/main/resources/
├── application.yaml       Ktor + app configuration
└── db/migration/          Flyway SQL migrations
src/test/kotlin/           tests + fakes
```

## Tech stack

Kotlin · Ktor · kotlinx.serialization · Exposed · PostgreSQL · Flyway ·
RabbitMQ · Koin · JUnit 5
