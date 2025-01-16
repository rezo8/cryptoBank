package bookkeeping

import collection.mutable.Stack
import org.scalatest._
import flatspec._
import matchers._
import scala.util.Random._

import bookkeeping.BookkeepingFixtures._

import java.util.UUID._
import models.TransactionChain

class TransactionChainSpec extends AnyFlatSpec with should.Matchers {

  it should "properly initialize an empty Transaction Chain" in {
    var transactionChain = new TransactionChain()
    transactionChain.removeHead() shouldBe None
    transactionChain.removeTail() shouldBe None
  }

  it should "properly prepend a transaction to the head of a transactionChain" in {
    var transactionChain = new TransactionChain()
    var firstTransaction = nextTransaction()
    var secondTransaction = nextTransaction()

    transactionChain.prepend(secondTransaction)
    transactionChain.prepend(firstTransaction)
    transactionChain.removeHead().get shouldBe firstTransaction
  }

  it should "properly append a transaction to the tail of a transactionChain" in {
    var transactionChain = new TransactionChain()
    var firstTransaction = nextTransaction()
    var secondTransaction = nextTransaction()

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
