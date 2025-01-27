CREATE TABLE IF NOT EXISTS userTypes (
    userTypeId SERIAL PRIMARY KEY,
    typeName VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS USERS(
    userId UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    userTypeId INT REFERENCES userTypes(userTypeId),
    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phoneNumber TEXT NOT NULL UNIQUE,
    passwordHash VARCHAR(255),
    createdAt TIMESTAMP DEFAULT current_timestamp,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO userTypes(userTypeId, typeName, description)
VALUES
    (1, 'ADMIN', 'System administrators with full access.'),
    (2, 'CUSTOMER', 'Regular users who can send and receive funds.'),
    (3, 'MERCHANT', 'Businesses that accept payments.');

CREATE INDEX idxUsersEmail ON users(email);
