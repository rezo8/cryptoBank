package models

import java.time.Instant
import java.util.UUID

case class Transaction(
    transactionId: UUID, // TODO build wrapper class.
    inputs: Map[
      UUID, // This is a wallet Id.
      TransactionValue
    ], // TODO build wrapper class around UUIDs
    outputs: Map[UUID, TransactionValue],
    transactionTime: Instant
) {

  def inputWalletIds(): Seq[UUID] = {
    inputs.keys.toSeq
  }

  def totalInputs(): TransactionValue =
    inputs.values.foldLeft[TransactionValue](fixedZero)((acc, curr) =>
      TransactionValue(curr.bitCoinChunk.+(acc.bitCoinChunk))
    )

  def outputWalletIds(): Seq[UUID] = {
    outputs.keys.toSeq
  }

  def totalOutputs(): TransactionValue =
    outputs.values.foldLeft[TransactionValue](fixedZero)((acc, curr) =>
      TransactionValue(curr.bitCoinChunk.+(acc.bitCoinChunk))
    )

  def transactionFee(): TransactionValue = {

    return TransactionValue(
      this.totalInputs().bitCoinChunk.-(this.totalOutputs().bitCoinChunk)
    )
  }

}
