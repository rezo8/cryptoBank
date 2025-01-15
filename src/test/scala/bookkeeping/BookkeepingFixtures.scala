package bookkeeping

import java.util.UUID
import java.util.UUID._

object BookkeepingFixtures {

  def nextTransactionValue(
      value: BigDecimal = BigDecimal(math.random())
  ): TransactionValue = {
    return TransactionValue(value)
  }

  def nextTransaction(
      inputs: Map[UUID, TransactionValue] = Map(
        randomUUID() -> nextTransactionValue(),
        randomUUID() -> nextTransactionValue()
      ),
      outputs: Map[UUID, TransactionValue] = Map(
        randomUUID() -> nextTransactionValue(0.10),
        randomUUID() -> nextTransactionValue(0.20)
      )
  ): Transaction = {
    return Transaction(inputs, outputs)
  }

  def nextWallet(
      uuid: UUID = java.util.UUID.randomUUID(),
      userId: UUID = java.util.UUID.randomUUID(),
      coins: Seq[UUID] = Seq()
  ): Wallet = {
    Wallet(uuid, userId, coins)
  }

}
