-- Create Account Table
CREATE TABLE accounts (
    accountId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    userId UUID REFERENCES users(userId),
    cryptoType VARCHAR(10) NOT NULL, -- can only have one cryptoType per account. Will need sep account for ETH, BTC etc...
    balance BIGINT DEFAULT 0,
    accountName VARCHAR(255),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_cryptoType UNIQUE (userId, cryptoType) -- Unique constraint
);
