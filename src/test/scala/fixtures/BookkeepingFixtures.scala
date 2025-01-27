package fixtures

import models.{Transaction, TransactionValue, Wallet}

import java.time.Instant
import java.util.UUID
import java.util.UUID.*
import scala.util.Random

object BookkeepingFixtures {

  def nextTransactionValue(
      value: BigDecimal = BigDecimal(math.random())
  ): TransactionValue = {
    TransactionValue(value)
  }

  // Arguable that the inputs and outputs in this are dumb as the transaction is invalid.
  // TODO fix to guarantee random transactions validity.
  def nextTransaction(
      transactionId: UUID = randomUUID(),
      inputs: Map[UUID, TransactionValue] = Map(
        randomUUID() -> nextTransactionValue(),
        randomUUID() -> nextTransactionValue()
      ),
      outputs: Map[UUID, TransactionValue] = Map(
        randomUUID() -> nextTransactionValue(),
        randomUUID() -> nextTransactionValue()
      ),
      changeAddressOpt: Option[UUID] = Some(randomUUID()),
      itemOfPurchase: String = Random.nextString(10),
      transactionTime: Instant = Instant.now()
  ): Transaction = {
    Transaction(
      transactionId,
      inputs,
      outputs,
      changeAddressOpt,
      itemOfPurchase,
      transactionTime
    )
  }

  def nextWallet(
      id: UUID = java.util.UUID.randomUUID(),
      userId: UUID = java.util.UUID.randomUUID(),
      currency: String = "BTC",
      balance: BigDecimal = BigDecimal(math.random()),
      walletName: String = Random.nextString(10),
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): Wallet = {
    Wallet(
      id = id,
      userId = userId,
      currency = currency,
      balance = balance,
      walletName = walletName,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
}
