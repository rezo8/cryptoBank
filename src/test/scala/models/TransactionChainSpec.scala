package models

import com.rezo.models.TransactionChain
import fixtures.BookkeepingFixtures.*
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.*

class TransactionChainSpec extends AnyFlatSpec with should.Matchers {

  it should "properly initialize an empty Transaction Chain" in {
    val transactionChain = new TransactionChain()
    transactionChain.removeHead() shouldBe None
    transactionChain.removeTail() shouldBe None
  }

  it should "properly prepend a transaction to the head of a transactionChain" in {
    val transactionChain = new TransactionChain()
    val firstTransaction = nextTransaction()
    val secondTransaction = nextTransaction()

    transactionChain.prepend(secondTransaction)
    transactionChain.prepend(firstTransaction)
    transactionChain.removeHead().get shouldBe firstTransaction
  }

  it should "properly append a transaction to the tail of a transactionChain" in {
    val transactionChain = new TransactionChain()
    val firstTransaction = nextTransaction()
    val secondTransaction = nextTransaction()

    transactionChain.append(firstTransaction)
    transactionChain.append(secondTransaction)
    transactionChain.removeTail().get shouldBe secondTransaction
  }

  it should "properly compute total transaction fees" in {

    var transactionChain = new TransactionChain()

    transactionChain.totalTransactionFees() shouldBe BigDecimal(0)

    // Test singleton case
    val transaction = nextTransaction()
    transactionChain.append(transaction)

    transactionChain
      .totalTransactionFees() shouldBe transaction.transactionFee().bitCoinChunk

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

    transactionChain.totalTransactionFees() shouldBe expectedFees
  }
}
