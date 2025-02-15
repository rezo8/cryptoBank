package com.rezo.models

val SatoshiPerBitcoin = BigDecimal(100_000_000L)
val fixedZero = TransactionValue(BigDecimal(0))

case class TransactionValue(bitCoinChunk: BigDecimal) {
  // TODO Add Specs
  def toSatoshis: BigDecimal = bitCoinChunk.pow(8)
}

object TransactionValue {
  def fromSatoshis(satoshis: BigDecimal): TransactionValue = {
    TransactionValue(satoshis.pow(-8))
  }
}
