CREATE TABLE shop_products (
    code        VARCHAR(80) PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    product_type VARCHAR(20) NOT NULL,
    category    VARCHAR(20) NOT NULL,
    item_type   VARCHAR(60),
    equipment_template_name VARCHAR(120),
    price       INT NOT NULL,
    sell_price  INT NOT NULL DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);
