# Task Manager

A REST API for managing tasks, built with Java 21 and Spring Boot.

## Features

Each task has:

| Field | Type | Description |
|---|---|---|
| `id` | Long | Generated identifier |
| `title` | String | Required title |
| `description` | String | Optional details |
| `status` | Enum | `PENDING`, `IN_PROGRESS`, or `COMPLETED` |
| `dueDate` | LocalDate | Optional due date (`YYYY-MM-DD`) |

Supported operations:

- Create a task (new tasks always start as `PENDING`)
- List all tasks
- Get a task by id
- Update title, description, and due date
- Change status
- Delete a task

Invalid requests are rejected: a missing title returns HTTP 400, and an unknown id returns HTTP 404.

## Technologies

| Technology | Role |
|---|---|
| Java 21 | Language |
| Spring Boot | Application framework |
| Spring Web (MVC) | REST API |
| Spring Data JPA | Persistence |
| H2 | In-memory database |
| Bean Validation | Request validation (`@NotBlank` on title) |
| Maven | Build and dependencies |
| JUnit 5 | Automated tests |
| Mockito | Isolating the service layer in unit tests |
| Spring Boot Test / MockMvc | Integration tests |
| Postman (optional) | Manual HTTP testing |

H2 runs inside the application. Data is stored only while the process is running and is lost when the app stops.

## Architecture

The application is organized in layers. Each package has a single responsibility:

```text
src/main/java/com/example/taskmanager/
├── controller/     # HTTP API (presentation)
├── service/        # Business rules
├── repository/     # Data access
└── model/          # Domain entities and enums
```

Request flow:

```text
Client (Postman / curl)
        │
        ▼
 TaskController     receives HTTP, returns JSON
        │
        ▼
  TaskService       validations and business rules
        │
        ▼
 TaskRepository     Spring Data JPA
        │
        ▼
   H2 Database
```

The response travels back along the same path.

How this maps to the design concepts:

- **Layered architecture** — controller, service, and repository are separate and can be changed or tested independently.
- **MVC** — the controller handles HTTP, `Task` is the model, and JSON is the view.
- **Repository** — `TaskRepository` extends `JpaRepository` and hides SQL from the service.
- **Client–server** — any HTTP client talks to `http://localhost:8080`.

## Requirements

- JDK 21
- Maven Wrapper (`mvnw` / `mvnw.cmd`) is included; a global Maven install is not required

If `java -version` shows an older JDK, point `JAVA_HOME` at JDK 21 before running Maven.

Windows example:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
```

## How to run

From the project root:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080/tasks`.

The H2 console is enabled at `http://localhost:8080/h2-console`. JDBC URL: `jdbc:h2:mem:taskdb`, user `sa`, empty password.

## API

Base URL: `http://localhost:8080`

| Method | Path | Description | Success status |
|---|---|---|---|
| `POST` | `/tasks` | Create a task | 201 |
| `GET` | `/tasks` | List all tasks | 200 |
| `GET` | `/tasks/{id}` | Get one task | 200 |
| `PUT` | `/tasks/{id}` | Update task data | 200 |
| `PATCH` | `/tasks/{id}/status` | Change status | 200 |
| `DELETE` | `/tasks/{id}` | Delete a task | 204 |

### Create a task

```http
POST /tasks
Content-Type: application/json
```

```json
{
  "title": "Study software architecture",
  "description": "Review architectural patterns",
  "dueDate": "2026-11-25"
}
```

Example response:

```json
{
  "id": 1,
  "title": "Study software architecture",
  "description": "Review architectural patterns",
  "status": "PENDING",
  "dueDate": "2026-11-25"
}
```

### Change status

```http
PATCH /tasks/1/status
Content-Type: application/json
```

```json
{
  "status": "COMPLETED"
}
```

Allowed values: `PENDING`, `IN_PROGRESS`, `COMPLETED`.

### curl examples

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Study tests\",\"description\":\"Practice JUnit\",\"dueDate\":\"2026-11-25\"}"

curl http://localhost:8080/tasks

curl http://localhost:8080/tasks/1

curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Updated title\",\"description\":\"Updated description\",\"dueDate\":\"2026-11-30\"}"

curl -X PATCH http://localhost:8080/tasks/1/status \
  -H "Content-Type: application/json" \
  -d "{\"status\":\"COMPLETED\"}"

curl -X DELETE http://localhost:8080/tasks/1
```

## Tests

Two kinds of automated tests are included:

| Test | Location | What it covers |
|---|---|---|
| Unit | `TaskServiceTest` | Business rules with a mocked repository (Mockito) |
| Integration | `TaskControllerTest` | Full HTTP flow through controller, service, repository, and H2 |

Run all tests:

```bash
./mvnw test
```

Windows:

```bash
mvnw.cmd test
```

System-level checks can be done with Postman by exercising the endpoints above, including invalid cases such as an empty title or a missing id.
