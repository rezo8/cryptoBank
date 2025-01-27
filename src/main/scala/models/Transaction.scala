package models

import java.time.Instant
import java.util.UUID

case class Transaction(
    transactionId: UUID, // TODO build wrapper class.
    inputs: Map[
      UUID, // This is a account Id.
      TransactionValue
    ], // TODO build wrapper class around UUIDs
    outputs: Map[UUID, TransactionValue],
    changeAddressOpt: Option[
      UUID
    ], // The ID of the address the change goes to if there is change. Will exist in the change address..
    itemOfPurchase: String,
    transactionTime: Instant
) {

  def inputAccountIds(): Seq[UUID] = {
    inputs.keys.toSeq
  }

  def totalInputs(): TransactionValue =
    inputs.values.foldLeft[TransactionValue](fixedZero)((acc, curr) =>
      TransactionValue(curr.bitCoinChunk.+(acc.bitCoinChunk))
    )

  def outputAccountIds(): Seq[UUID] = {
    outputs.keys.toSeq
  }

  def totalOutputs(): TransactionValue =
    outputs.values.foldLeft[TransactionValue](fixedZero)((acc, curr) =>
      TransactionValue(curr.bitCoinChunk.+(acc.bitCoinChunk))
    )

  def nonChangeOutputs(): TransactionValue =
    outputs.foldLeft[TransactionValue](fixedZero)((acc, curr) => {
      if (Some(curr._1) != this.changeAddressOpt) {
        TransactionValue(curr._2.bitCoinChunk.+(acc.bitCoinChunk))
      } else {
        acc
      }
    })

  def getChange(): Option[TransactionValue] = {
    this.changeAddressOpt.fold[Option[TransactionValue]](None)(
      changeAddress => {
        this.outputs.get(changeAddress)
      }
    )
  }

  def transactionFee(): TransactionValue = {
    return TransactionValue(
      this
        .totalInputs()
        .bitCoinChunk
        .-(this.nonChangeOutputs().bitCoinChunk)
    )
  }

}
