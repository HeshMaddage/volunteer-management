# Volunteer Management System

A volunteer management app: volunteer profiles with skills, events
with multiple shifts, capacity-limited registration, attendance tracking,
and cumulative hours logging.

> Status: 🚧 In active development
## Problem

Nonprofits need a simple way to: post events with time-slot shifts, let
volunteers browse and sign up (without overbooking a shift), track who
actually showed up, and give volunteers a running total of hours contributed.

## Core Entities

- **User** — auth identity, role `ADMIN` or `VOLUNTEER`
- **VolunteerProfile** — 1:1 with User; name, contact info, skills, bio
- **Skill** — tag, many-to-many with VolunteerProfile and Shift
- **Event** — title, description, location, status
- **Shift** — a time slot within an event; capacity + required skills
- **Registration** — a volunteer signed up for a shift; status lifecycle
- **HoursLog** — hours credited after a shift is marked attended

## Tech Stack

- Java 21, Spring Boot 3.3
- PostgreSQL + Flyway migrations
- Spring Security (JWT)
- Docker / Docker Compose
- Testcontainers (integration tests)
- GitHub Actions (CI/CD)

## Running Locally

```bash
cp .env.example .env
docker compose up --build
```

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## License

MIT — see `LICENSE`.
