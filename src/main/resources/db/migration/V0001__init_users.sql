CREATE TABLE IF NOT EXISTS USERS(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phoneNumber TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT current_timestamp
);


