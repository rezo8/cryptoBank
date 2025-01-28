## Crypto Bank Project

### Description

This project is a basic bank simulator with the purpose of storing fake "bitcoins".

It handles deposits, returns and transfers, both intra and inter bank.

This is a basic bank that does not operate in the real "banking" world.

The goal long term is not just to store internal transfers, but also to populate a transfer ledger to closer simulate Crypto Blockchain operations.

### Usage

#### Running in Docker

**Requirements:**

1. Docker :D

**Commands to Run from project dir (i.e., `bitcoin/`):**

`docker-compose up --build`

If running into issues, reset by running:

`docker-compose down -v`

#### Running Locally

**Requirements:**

1. Docker
2. Flyway
3. SBT

**Commands to Run from project dir (i.e., `bitcoin/`):**

1. Start PostgreSQL container:
   `docker run --name postgres-container -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=mypassword -e POSTGRES_DB=postgres -p 5432:5432 -d postgres`

2. Run Flyway migrations:
   `flyway migrate -configFiles=./flyway/flyway.conf`

3. Compile the project:
   `sbt compile`

4. Run the application:
   `sbt run`

Enjoy!
