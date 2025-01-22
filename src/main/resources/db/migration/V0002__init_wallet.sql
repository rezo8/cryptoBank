-- Create Wallets Table
CREATE TABLE IF NOT EXISTS WALLETS(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ownerId UUID NOT NULL UNIQUE,
    walletName VARCHAR(255),
    CONSTRAINT fk_user FOREIGN KEY (ownerId) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Coins Table
CREATE TABLE coins (
    coinId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    walletId UUID NOT NULL,
    coinName VARCHAR(255) NOT NULL,
    satoshiAmount NUMERIC(21, 8) NOT NULL,
    CONSTRAINT fk_wallet FOREIGN KEY (walletId) REFERENCES wallets(id) ON DELETE CASCADE
);

