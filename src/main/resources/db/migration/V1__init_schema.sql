CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'VOLUNTEER')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE volunteer_profiles (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    full_name    VARCHAR(255) NOT NULL,
    phone        VARCHAR(50),
    address      VARCHAR(500),
    bio          TEXT,
    join_date    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE skills (
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name   VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE volunteer_skills (
    volunteer_id   UUID NOT NULL REFERENCES volunteer_profiles(id) ON DELETE CASCADE,
    skill_id       UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (volunteer_id, skill_id)
);

CREATE TABLE events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    location      VARCHAR(255),
    status        VARCHAR(20) NOT NULL DEFAULT 'UPCOMING'
                      CHECK (status IN ('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_status ON events(status);

CREATE TABLE shifts (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id     UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    start_time   TIMESTAMPTZ NOT NULL,
    end_time     TIMESTAMPTZ NOT NULL,
    capacity     INTEGER NOT NULL CHECK (capacity >= 1),
    CONSTRAINT chk_shift_time_order CHECK (end_time > start_time)
);

CREATE INDEX idx_shifts_event ON shifts(event_id);

CREATE TABLE shift_required_skills (
    shift_id   UUID NOT NULL REFERENCES shifts(id) ON DELETE CASCADE,
    skill_id   UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (shift_id, skill_id)
);

CREATE TABLE registrations (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    volunteer_id     UUID NOT NULL REFERENCES volunteer_profiles(id) ON DELETE CASCADE,
    shift_id         UUID NOT NULL REFERENCES shifts(id) ON DELETE CASCADE,
    status           VARCHAR(20) NOT NULL DEFAULT 'REGISTERED'
                         CHECK (status IN ('REGISTERED', 'ATTENDED', 'NO_SHOW', 'CANCELLED')),
    registered_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- A volunteer can only hold ONE active (non-cancelled) registration per
-- shift. Using a partial unique index (not a plain UNIQUE constraint) so
-- that cancelling and re-registering for the same shift is still possible —
-- a plain constraint on (volunteer_id, shift_id) would permanently block
-- that shift for that volunteer after a single cancellation.
CREATE UNIQUE INDEX uq_active_registration
    ON registrations (volunteer_id, shift_id)
    WHERE status IN ('REGISTERED', 'ATTENDED');

CREATE INDEX idx_registrations_shift ON registrations(shift_id);
CREATE INDEX idx_registrations_volunteer ON registrations(volunteer_id);

CREATE TABLE hours_logs (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registration_id    UUID NOT NULL UNIQUE REFERENCES registrations(id) ON DELETE CASCADE,
    hours              DOUBLE PRECISION NOT NULL CHECK (hours >= 0),
    logged_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
