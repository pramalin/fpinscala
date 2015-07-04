package fpinscala.parser

import fpinscala.collection.list.MyList
import fpinscala.either.{MyLeft, MyRight}
import fpinscala.parser.{ Reference => R }
import org.scalatest.{DiagrammedAssertions, FlatSpec}

class ParserReferenceSpec extends FlatSpec with DiagrammedAssertions {

  import ReferenceTypes._
  import Reference._

  val R = Reference

  "firstNonmatchingIndex" should "return the first index where the two strings differed" in {
    assert(firstNonmatchingIndex("hello", "ello", 0) === 0)
    assert(firstNonmatchingIndex("hello", "hollo", 0) === 1)

    assert(firstNonmatchingIndex("hello", "llo", 1) === 0)
    assert(firstNonmatchingIndex("hello", "hello", 0) === -1)
    assert(firstNonmatchingIndex("hello", "ello", 1) === -1)
  }

  "string" should "succeed parse a string" in {
    val MyRight(result) = R.run(string("hello"))("hello")
    assert(result == "hello")
  }

  "`p | q`" should "succeed if `p` succeed or `q` succeed" in {
    val parser = or(string("hello"), string("world"))
    val MyRight(hello) = R.run(parser)("hello")
    val MyRight(world) = R.run(parser)("world")
    assert(hello === "hello")
    assert(world === "world")
  }

  "`p | q`" should "fail if `p` fail and `q` fail" in {
    val parser = or(string("hello"), string("world"))
    val MyLeft(pr) = R.run(parser)("hard")
    val MyLeft(qr) = R.run(parser)("work")
    assert(pr.latestLocation.get.offset === 1)
    assert(qr.latestLocation.get.offset === 3)
  }

  "p.flatMap(q)" should "return a result depend on result p" in {
    val parser = regex("[0-9]".r).flatMap { n =>
      string("a" * n.toInt)
    }
    val MyRight(result) = R.run(parser)("4aaaa")
    assert(result === "aaaa")
  }

  "`p.map(f)`" should "parse and apply f to parsed result" in {
    val parser = string("hard").map(_.toUpperCase)
    val MyRight(result) = R.run(parser)("hard")
    assert(result === "HARD")
  }

  "`p ** q`" should "product " in {
    //val spaces = many(string(" "))
    val spaces = string("   ")
    val parser = product(spaces, string("abra"))
    val MyRight(result) = R.run(parser)("   abra")
    assert(result === "   " -> "abra")
  }

}
