CREATE TABLE IF NOT EXISTS book
(
    isbn TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    subject TEXT NOT NULL,
    overview TEXT,
    publisher TEXT NOT NULL,
    publication_date Date NOT NULL,
    lang TEXT NOT NULL
);