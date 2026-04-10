CREATE SCHEMA accessibility_map;
CREATE TABLE accessibility_map.distributed_locks
(
    lock_name   VARCHAR(255) PRIMARY KEY,
    owner_id    VARCHAR(255),
    lock_count  INT       DEFAULT 1,
    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lock_expiry TIMESTAMP
);
