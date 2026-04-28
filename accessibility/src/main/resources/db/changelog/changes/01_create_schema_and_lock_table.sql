CREATE SCHEMA accessibility_map;
CREATE TABLE accessibility_map.distributed_locks
(
    lock_name   VARCHAR(255) PRIMARY KEY,
    owner_id uuid,
    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lock_expiry TIMESTAMP
);
