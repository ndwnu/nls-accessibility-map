ALTER TABLE accessibility_map.active_version
DROP CONSTRAINT active_version_name_cache_version_unique;

ALTER TABLE accessibility_map.active_version
DROP CONSTRAINT active_version_pkey;

ALTER TABLE accessibility_map.active_version
    ADD CONSTRAINT active_version_pkey PRIMARY KEY (name, cache_version);


