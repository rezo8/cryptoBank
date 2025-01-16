package bookkeeping

import java.util.UUID
import java.util.UUID._
import java.time.Instant

object BookkeepingFixtures {

  def nextTransactionValue(
      value: BigDecimal = BigDecimal(math.random())
  ): TransactionValue = {
    return TransactionValue(value)
  }

  def nextTransaction(
      transactionId: UUID = randomUUID(),
      inputs: Map[UUID, TransactionValue] = Map(
        randomUUID() -> nextTransactionValue(),
        randomUUID() -> nextTransactionValue()
      ),
      outputs: Map[UUID, TransactionValue] = Map(
        randomUUID() -> nextTransactionValue(0.10),
        randomUUID() -> nextTransactionValue(0.20)
      ),
      transactionTime: Instant = Instant.now()
  ): Transaction = {
    return Transaction(
      transactionId,
      inputs,
      outputs,
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
