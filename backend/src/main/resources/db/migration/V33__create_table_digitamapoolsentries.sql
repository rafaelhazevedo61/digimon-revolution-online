CREATE TABLE digitama_pool_entries (
    id BIGSERIAL PRIMARY KEY,
    digitama_pool_id BIGINT NOT NULL REFERENCES digitama_pools(id),
    digimon_info_id BIGINT NOT NULL REFERENCES digimon_infos(id),
    weight INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (digitama_pool_id, digimon_info_id)
);
