package models

case class CoinValue(satoshis: Long) {
  require(
    satoshis >= 0 && satoshis < 100_000_000,
    "Coin value must be between 0 and less than 100,000,000 satoshis."
  )
}
