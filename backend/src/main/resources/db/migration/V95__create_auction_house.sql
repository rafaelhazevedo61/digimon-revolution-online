CREATE TABLE auction_listings (
    id UUID PRIMARY KEY,
    seller_player_id UUID NOT NULL REFERENCES players(id),
    item_definition_id BIGINT NOT NULL REFERENCES item_definitions(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    remaining_quantity INT NOT NULL CHECK (remaining_quantity >= 0 AND remaining_quantity <= quantity),
    unit_price INT NOT NULL CHECK (unit_price > 0),
    listing_fee INT NOT NULL CHECK (listing_fee >= 0),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_auction_listing_expiration CHECK (expires_at > created_at)
);

CREATE INDEX idx_auction_listings_search
    ON auction_listings(status, expires_at, item_definition_id, created_at DESC);
CREATE INDEX idx_auction_listings_seller
    ON auction_listings(seller_player_id, created_at DESC);

CREATE TABLE auction_transactions (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL REFERENCES auction_listings(id),
    seller_player_id UUID NOT NULL REFERENCES players(id),
    buyer_player_id UUID NOT NULL REFERENCES players(id),
    item_definition_id BIGINT NOT NULL REFERENCES item_definitions(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price INT NOT NULL CHECK (unit_price > 0),
    gross_amount INT NOT NULL CHECK (gross_amount > 0),
    fee INT NOT NULL CHECK (fee >= 0),
    seller_net_amount INT NOT NULL CHECK (seller_net_amount >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auction_transactions_buyer
    ON auction_transactions(buyer_player_id, created_at DESC);
CREATE INDEX idx_auction_transactions_seller
    ON auction_transactions(seller_player_id, created_at DESC);
CREATE INDEX idx_auction_transactions_listing
    ON auction_transactions(listing_id, created_at DESC);
