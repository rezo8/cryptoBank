## Crypto Bank Project

### Description

This project is a basic bank simulator with the purpose of storing fake "bitcoins".

It handles deposits, returns and transfers, both intra and inter bank.

This is a basic bank that does not operate in the real "banking" world. 

The goal long term is not just to store internal transfers, but also to populate a transfer ledger to closer simulate Crypto Blockchain operations.

### Usage

This project has a database dependency. To run it, you need docker installed with a postgres image downloaded.

Then, run: docker run --name postgres-container -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=postgres -p 5432:5432 -d postgres

Afterwards, the app is set up for application.conf to work. 
Feel free to edit variables as necessary to suit your usecase.

Once that is set up, `sbt compile` and `sbt run`  will start the service.

SBT can be installed with brew.

NOTE: Trying to move to a containerized version so people don't need to download sbt.
