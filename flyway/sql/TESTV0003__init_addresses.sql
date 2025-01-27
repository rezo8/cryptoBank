CREATE TABLE addresses (
    addressId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accountId UUID REFERENCES accounts(accountId),
    address VARCHAR(255) NOT NULL,
    balance BIGINT DEFAULT 0,
    isActive BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
