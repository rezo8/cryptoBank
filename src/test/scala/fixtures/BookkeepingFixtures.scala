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
    return TransactionValue(value)
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
    return Transaction(
      transactionId,
      inputs,
      outputs,
      changeAddressOpt,
      itemOfPurchase,
      transactionTime
    )
  }

  def nextWallet(
      uuid: UUID = java.util.UUID.randomUUID(),
      userId: UUID = java.util.UUID.randomUUID(),
      coins: Seq[UUID] = Seq()
  ): Wallet = {
    Wallet(uuid, userId, coins)
  }
}
