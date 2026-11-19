ALTER TABLE auction_listings
    ADD COLUMN seller_digimon_id UUID REFERENCES digimons(id),
    ADD COLUMN seller_fee_rate_bps INT NOT NULL DEFAULT 500;

ALTER TABLE auction_listings
    ADD CONSTRAINT chk_auction_listing_fee_rate_bps
    CHECK (seller_fee_rate_bps >= 0 AND seller_fee_rate_bps <= 10000);

CREATE INDEX idx_auction_listings_expiration
    ON auction_listings(status, expires_at)
    WHERE status = 'ACTIVE' AND remaining_quantity > 0;

UPDATE auction_listings listing
SET seller_digimon_id = player.active_digimon_id
FROM players player
WHERE listing.seller_player_id = player.id
  AND listing.seller_digimon_id IS NULL
  AND player.active_digimon_id IS NOT NULL;
