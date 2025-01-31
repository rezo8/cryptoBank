package services

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalamock.stubs.Stubs
import services.Greetings.Formatter

import scala.annotation.experimental

object Greetings {
  trait Formatter { def format(s: String): String }
  object EnglishFormatter extends Formatter {
    def format(s: String): String = s"Hello $s"
  }
  object GermanFormatter extends Formatter {
    def format(s: String): String = s"Hallo $s"
  }
  object JapaneseFormatter extends Formatter {
    def format(s: String): String = s"こんにちは $s"
  }

  def sayHello(name: String, formatter: Formatter): Unit = {
    println(formatter.format(name))
  }
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

@experimental
class ReallySimpleExampleTest extends AnyFunSuite with MockFactory {
  test("sayHello") {
    val mockFormatter = mock[Formatter]

    mockFormatter.format
      .expects("Mr Bond")
      .returning("Ah, Mr Bond. I've been expecting you")
      .once()

    Greetings.sayHello("Mr Bond", mockFormatter)
  }
}
