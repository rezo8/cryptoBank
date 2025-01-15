// For more information on writing tests, see
import bookkeeping._
import bookkeeping.BookkeepingFixtures._

import java.util.UUID._
// https://scalameta.org/munit/docs/getting-started.html
class BookkeepingSuite extends munit.FunSuite {
  import scala.util.Random._

  test("transactionFee") {
    val inputs = Map(
      randomUUID() -> nextTransactionValue(0.10),
      randomUUID() -> nextTransactionValue(0.20),
      randomUUID() -> nextTransactionValue(0.10),
      randomUUID() -> nextTransactionValue(0.15)
    )
    val outputs = Map(
      randomUUID() -> nextTransactionValue(0.10),
      randomUUID() -> nextTransactionValue(0.20),
      randomUUID() -> nextTransactionValue(0.20)
    )
    val transaction = nextTransaction(inputs, outputs)
    val expectedFee = nextTransactionValue(0.05)

    assertEquals(transaction.transactionFee(), expectedFee)
  }
}
