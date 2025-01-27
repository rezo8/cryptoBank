-- Create Wallets Table
CREATE TABLE IF NOT EXISTS WALLETS(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    userId UUID NOT NULL UNIQUE,
    walletName VARCHAR(255),
    CONSTRAINT fk_user FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);
