// For more information on writing tests, see
import bookkeeping._
import bookkeeping.BookkeepingFixtures._

import java.util.UUID._
// https://scalameta.org/munit/docs/getting-started.html
class TransactionSuiteSuite extends munit.FunSuite {
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

  // TODO create an actual test suite.
  test("initialize TransactionChain") {
    var transactionChain = new TransactionChain()
    assertEquals(None, transactionChain.removeHead())
    assertEquals(None, transactionChain.removeTail())
  }

  test("TransactionChain::prepend") {

    var transactionChain = new TransactionChain()
    var firstTransaction = nextTransaction()
    var secondTransaction = nextTransaction()

    transactionChain.prepend(firstTransaction)
    transactionChain.append(secondTransaction)
    assertEquals(firstTransaction, transactionChain.removeHead().get)
  }

  test("TransactionChain::append") {
    var transactionChain = new TransactionChain()
    var firstTransaction = nextTransaction()
    var secondTransaction = nextTransaction()

    transactionChain.append(firstTransaction)
    transactionChain.append(secondTransaction)
    assertEquals(secondTransaction, transactionChain.removeTail().get)
  }

  test("TransactionChain::totalTransactionFees") {
    // Test singleton case

    var transactionChain = new TransactionChain()
    assertEquals(
      transactionChain.totalTransactionFees(),
      BigDecimal(0)
    )

    // Test singleton case
    val transaction = nextTransaction()
    transactionChain.append(transaction)

    assertEquals(
      transactionChain.totalTransactionFees(),
      transaction.transactionFee().bitCoinChunk
    )

    // Test multiple case
    val transactions = List(
      nextTransaction(),
      nextTransaction(),
      nextTransaction(),
      nextTransaction()
    )

    val expectedFees: BigDecimal =
      transactions.foldLeft[BigDecimal](BigDecimal(0))((acc, curr) =>
        acc.+(curr.transactionFee().bitCoinChunk)
      )

    transactionChain = new TransactionChain()
    transactions.foreach(transactionChain.append(_))

    assertEquals(transactionChain.totalTransactionFees(), expectedFees)
  }

}
