package bookkeeping

import org.scalatest.*
import flatspec.*
import matchers.*
import bookkeeping.BookkeepingFixtures.*
import models.{Transaction, TransactionValue}

import java.util.UUID
import java.util.UUID.*

class TransactionSpec extends AnyFlatSpec with should.Matchers {

  val changeId: UUID = randomUUID()
  val inputs: Map[UUID, TransactionValue] = Map(
    randomUUID() -> nextTransactionValue(0.10),
    randomUUID() -> nextTransactionValue(0.20),
    randomUUID() -> nextTransactionValue(0.10),
    randomUUID() -> nextTransactionValue(0.15)
  )
  val outputs: Map[UUID, TransactionValue] = Map(
    randomUUID() -> nextTransactionValue(0.10),
    randomUUID() -> nextTransactionValue(0.20),
    randomUUID() -> nextTransactionValue(0.20),
    changeId -> nextTransactionValue(0.005)
  )
  val defaultTransaction: Transaction =
    nextTransaction(inputs = inputs, outputs = outputs)

  it should "correctly evaluate a transaction Fee with change" in {
    defaultTransaction.transactionFee() should be(nextTransactionValue(0.045))
  }

  it should "correctly return change value" in {
    defaultTransaction.getChange().contains(nextTransactionValue(0.005))
  }

  it should "correctly sum all outputs" in {
    defaultTransaction.totalOutputs() should be(nextTransactionValue(0.505))
  }

  it should "correctly sum all inputs" in {
    defaultTransaction.totalInputs() should be(nextTransactionValue(0.55))
  }

  it should "correctly return all output Ids" in {
    defaultTransaction.outputWalletIds().sorted should be(
      outputs.keys.toSeq.sorted
    )
  }

  it should "correctly return all input Ids" in {
    defaultTransaction.inputWalletIds().sorted should be(
      inputs.keys.toSeq.sorted
    )
    print("passed")
  }
}
