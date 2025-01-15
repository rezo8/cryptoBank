package bookkeeping
import java.util.UUID

object BookkeepingFixtures {

  def nextTransactionValue(
      value: BigDecimal = BigDecimal(math.random())
  ): TransactionValue = {
    return TransactionValue(value)
  }

  def nextTransaction(
      inputs: Map[UUID, TransactionValue] = Map(),
      outputs: Map[UUID, TransactionValue] = Map()
  ): Transaction = {
    return Transaction(inputs, outputs)
  }

  def nextWallet(
      uuid: UUID = java.util.UUID.randomUUID(),
      userId: UUID = java.util.UUID.randomUUID()
  ): Wallet = {
    Wallet(uuid, userId)
  }
}
