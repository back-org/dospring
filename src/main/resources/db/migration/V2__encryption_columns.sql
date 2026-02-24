-- V4+ encryption-at-rest schema adjustments (PostgreSQL).
--
-- We increase column sizes to safely store AES-GCM ciphertext (base64).
-- NOTE: If you use MySQL, adapt these ALTER statements accordingly.

ALTER TABLE users
  ALTER COLUMN email TYPE VARCHAR(512);

ALTER TABLE user_order
  ALTER COLUMN razorpay_payment_id TYPE VARCHAR(512),
  ALTER COLUMN razorpay_signature TYPE VARCHAR(512);
