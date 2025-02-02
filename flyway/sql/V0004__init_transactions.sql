CREATE TABLE transactions (
    transactionId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    addressId UUID REFERENCES addresses(addressId),
    type VARCHAR(50) NOT NULL, -- e.g., DEPOSIT, WITHDRAWAL, TRANSFER
    amount DECIMAL(20, 8) NOT NULL,
    fee DECIMAL(20, 8) DEFAULT 0.0,
    status VARCHAR(50) DEFAULT 'PENDING', -- e.g., PENDING, CONFIRMED, FAILED
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE transactionAddresses (
    transactionAddressId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transactionId UUID REFERENCES transactions(transactionId),
    address VARCHAR(255) NOT NULL,
    addressType VARCHAR(10) NOT NULL CHECK (addressType IN ('FROM', 'TO')), -- FROM or TO
    isChange BOOLEAN DEFAULT False,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_transactions_addressId ON transactions(addressId);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactionAddresses_transactionId ON transactionAddresses(transactionId);
CREATE INDEX idx_transactionAddresses_transactionId_addressType ON transactionAddresses(transactionId, addressType);
