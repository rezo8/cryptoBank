-- Create Wallets Table
CREATE TABLE wallets (
    walletId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    userId UUID REFERENCES users(userId),
    currency VARCHAR(10) NOT NULL,
    balance BIGINT DEFAULT 0,
    walletName VARCHAR(255),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_currency UNIQUE (userId, currency) -- Unique constraint
);
