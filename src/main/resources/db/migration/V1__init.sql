-- V1: initial schema (PostgreSQL)
-- Core security tables + minimal domain tables.
-- NOTE: Keep schema changes via Flyway migrations in production.

CREATE TABLE IF NOT EXISTS roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(120) NOT NULL,
  password VARCHAR(200) NOT NULL,
  failed_login_attempts INT NOT NULL DEFAULT 0,
  lock_until TIMESTAMPTZ NULL,
  password_changed_at TIMESTAMPTZ NULL,
  last_login_at TIMESTAMPTZ NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT uk_users_username UNIQUE (username),
  CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS password_history (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  password_hash VARCHAR(200) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  device_id VARCHAR(120),
  user_agent VARCHAR(300),
  ip_address VARCHAR(60),
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ NULL,
  last_used_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_device ON refresh_tokens(device_id);

-- Domain tables (minimal)
CREATE TABLE IF NOT EXISTS flight_data (
  flight_id BIGSERIAL PRIMARY KEY,
  flight_number INT,
  departure_time VARCHAR(30),
  arrival_time VARCHAR(30),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS booking (
  booking_id BIGSERIAL PRIMARY KEY,
  departure_date TIMESTAMPTZ NULL,
  booking_date DATE NULL,
  flight_id BIGINT NULL,
  total_amount DOUBLE PRECISION NULL,
  otp INT NULL,
  booking_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
  checked_in BOOLEAN NOT NULL DEFAULT FALSE,
  payment_completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT fk_booking_flightdata FOREIGN KEY (flight_id) REFERENCES flight_data(flight_id)
);

CREATE TABLE IF NOT EXISTS passenger (
  passenger_id BIGSERIAL PRIMARY KEY,
  passenger_name VARCHAR(120),
  passenger_age INT,
  passenger_seat INT,
  amount DOUBLE PRECISION,
  booking_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT fk_passenger_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flights (
  id BIGSERIAL PRIMARY KEY,
  departure_location VARCHAR(120),
  arrival_location VARCHAR(120),
  departure_date VARCHAR(30),
  arrival_date VARCHAR(30),
  departure_time VARCHAR(30),
  arrival_time VARCHAR(30),
  total_seats INT,
  available_seats INT,
  price DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS user_order (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NULL,
  razorpay_payment_id VARCHAR(120),
  razorpay_order_id VARCHAR(120) NOT NULL,
  razorpay_signature VARCHAR(200),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_razorpay_order_id ON user_order(razorpay_order_id);
