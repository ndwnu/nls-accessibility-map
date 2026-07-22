ALTER TABLE accessibility_map.active_version
    ADD cache_version INT NOT NULL DEFAULT 3;

ALTER TABLE accessibility_map.active_version
    ADD CONSTRAINT active_version_name_cache_version_unique UNIQUE (name, cache_version)