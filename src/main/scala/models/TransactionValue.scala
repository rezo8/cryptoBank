package models

val SatoshiPerBitcoin = BigDecimal(100_000_000L)
val fixedZero = TransactionValue(BigDecimal(0))

case class TransactionValue(bitCoinChunk: BigDecimal) {
  def toSatoshis = bitCoinChunk.*(SatoshiPerBitcoin)
}
