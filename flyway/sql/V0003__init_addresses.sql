CREATE TABLE addresses (
    addressId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accountId UUID REFERENCES accounts(accountId),
    address VARCHAR(255) NOT NULL,
    balance BIGINT DEFAULT 0,
    isActive BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_accountId_address UNIQUE (accountId, address)
);

CREATE INDEX idxAddressesWalletId ON addresses(accountId);
