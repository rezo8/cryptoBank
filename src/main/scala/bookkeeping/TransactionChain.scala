package bookkeeping

// Unfortunately transaction chains are inherently mutable so they don't fit
// great in the Scala Ecosystem of things. So here is a kind of homegrown DoublyLinkedList
// to avoid importing deprecated classes

// Node definition for the doubly linked list
case class Node[A](
    var value: A,
    var prev: Option[Node[A]] = None,
    var next: Option[Node[A]] = None
)

// Doubly Linked List implementation
class TransactionChain {
  private var head: Option[Node[Transaction]] = None
  private var tail: Option[Node[Transaction]] = None

  // Check if the list is empty
  def isEmpty: Boolean = head.isEmpty

  // Add an element to the front of the list
  def prepend(value: Transaction): Unit = {
    val newNode = Node(value, prev = None, next = head)
    head.foreach(_.prev = Some(newNode))
    head = Some(newNode)
    if (tail.isEmpty) tail = head
  }

  // Add an element to the end of the list
  def append(value: Transaction): Unit = {
    val newNode = Node(value, prev = tail, next = None)
    tail.foreach(_.next = Some(newNode))
    tail = Some(newNode)
    if (head.isEmpty) head = tail
  }

  // Remove an element from the front of the list
  def removeHead(): Option[Transaction] = {
    head.map { h =>
      head = h.next
      head.foreach(_.prev = None)
      if (head.isEmpty) tail = None
      h.value
    }
  }

  // Remove an element from the end of the list
  def removeTail(): Option[Transaction] = {
    tail.map { t =>
      tail = t.prev
      tail.foreach(_.next = None)
      if (tail.isEmpty) head = None
      t.value
    }
  }

  // Traverse the list forward and apply a function to each element
  def traverseForward(f: Transaction => Unit): Unit = {
    var current = head
    while (current.isDefined) {
      f(current.get.value)
      current = current.get.next
    }
  }

  // Traverse the list backward and apply a function to each element
  def traverseBackward(f: Transaction => Unit): Unit = {
    var current = tail
    while (current.isDefined) {
      f(current.get.value)
      current = current.get.prev
    }
  }

  def totalTransactionFees(): BigDecimal = {
    var sum: BigDecimal = BigDecimal(0)

    traverseForward(transaction => {
      sum = sum.+(transaction.transactionFee().bitCoinChunk)
    })

    return sum
  }
}

case class TransactionChainNode[Transaction](
    var value: Transaction,
    var head: Option[TransactionChainNode[Transaction]] = None,
    var tail: Option[TransactionChainNode[Transaction]] = None
)
