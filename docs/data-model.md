# Data Model

```mermaid
erDiagram
    USERS ||--o| VOLUNTEER_PROFILES : has
    VOLUNTEER_PROFILES }o--o{ SKILLS : has
    SHIFTS }o--o{ SKILLS : requires
    EVENTS ||--o{ SHIFTS : contains
    VOLUNTEER_PROFILES ||--o{ REGISTRATIONS : makes
    SHIFTS ||--o{ REGISTRATIONS : receives
    REGISTRATIONS ||--o| HOURS_LOGS : produces

    USERS {
        uuid id PK
        string email
        string password_hash
        string role
        timestamp created_at
    }

    VOLUNTEER_PROFILES {
        uuid id PK
        uuid user_id FK
        string full_name
        string phone
        string address
        string bio
        timestamp join_date
    }

    SKILLS {
        uuid id PK
        string name
    }

    EVENTS {
        uuid id PK
        string title
        string description
        string location
        string status
        timestamp created_at
    }

    SHIFTS {
        uuid id PK
        uuid event_id FK
        timestamp start_time
        timestamp end_time
        int capacity
    }

    REGISTRATIONS {
        uuid id PK
        uuid volunteer_id FK
        uuid shift_id FK
        string status
        timestamp registered_at
    }

    HOURS_LOGS {
        uuid id PK
        uuid registration_id FK
        double hours
        timestamp logged_at
    }
```

## Design notes

- **Partial unique index on `(volunteer_id, shift_id)` on `registrations`**
  (only over `REGISTERED`/`ATTENDED` rows) — prevents a volunteer from
  double-registering for the same *active* shift at the DB level, while
  still allowing them to cancel and later re-register for that same shift
  (a plain, non-partial unique constraint would permanently block that).
- **Capacity lives on `Shift`, not `Event`** — an event can have multiple
  shifts (e.g., a morning and afternoon slot) each with independent capacity.
- **`HoursLog` is 1:1 with `Registration`**, created only when a registration
  transitions to `ATTENDED` — hours are derived from the shift's actual
  start/end time, not manually entered, so they can't be fabricated.
- **`Skill` is a shared lookup table**, many-to-many with both volunteers
  (what they can do) and shifts (what's required) — lets you match/filter
  later without duplicating skill names as free text.
