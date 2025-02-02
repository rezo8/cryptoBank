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

### Bank Design

#### Transaction Flow 

##### Asynchronous flow. Done for values >= 100 Satoshis
1. Transaction gets created. Set to status pending
2. Transaction Event gets published. 
3. Transaction Event Consumed. The Transaction Addresses are updated accordingly.
4. On success, sets to confirmed. 
5. On failure, set to failure.
6. Notify customer through text about the success/failure (not set up)

##### Synchronous flow. Done for values less than 100 Satoshis
1. Transaction gets created. Set to status pending
2. The Transaction Addresses are updated accordingly.
3. On success, sets to confirmed.
4. On failure, set to failure. Error response returned.
