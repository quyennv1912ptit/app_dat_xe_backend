-- Apply once before deploying the endpoints below. IF NOT EXISTS keeps existing tables intact.
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_image_url VARCHAR(2048);

CREATE TABLE IF NOT EXISTS ride_ratings (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT NOT NULL UNIQUE REFERENCES rides(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    tip BIGINT NOT NULL DEFAULT 0 CHECK (tip >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_ride_ratings_driver ON ride_ratings(driver_id);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT NOT NULL UNIQUE REFERENCES rides(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    amount BIGINT NOT NULL CHECK (amount >= 0),
    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    ride_id BIGINT REFERENCES rides(id),
    type VARCHAR(30) NOT NULL,
    amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_driver_created
    ON wallet_transactions(driver_id, created_at DESC);

CREATE TABLE IF NOT EXISTS notification_history (
    id BIGSERIAL PRIMARY KEY,
    recipient_role VARCHAR(20) NOT NULL,
    recipient_id BIGINT NOT NULL,
    message_id VARCHAR(64) NOT NULL UNIQUE,
    payload_type VARCHAR(50) NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notification_history_recipient_created
    ON notification_history(recipient_role, recipient_id, created_at DESC);
