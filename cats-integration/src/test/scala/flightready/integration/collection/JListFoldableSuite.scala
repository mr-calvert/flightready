package flightready.integration.collection

import scala.collection.JavaConverters

import cats._

import cats.laws.discipline.FoldableTests
import cats.tests.CatsSuite

import flightready.integration.cats.implicits._

/** Ensuring [[JListFoldable]] makes a lawful [[cats.Foldable]]
  *
  * [[JListFoldable]] supports [[cats.Foldable]] instances via
  * [[flightready.integration.collection.HiddenIterable]] and
  * [[flightready.integration.collection.HiddenRandomAccess]]. This
  * test applies Cats' Foldable laws to JListFoldable, ensuring its
  * lawfulness and the compilability of the implicit stack.
  */
class JListFoldableSuite extends CatsSuite {
  import org.scalacheck.Arbitrary

  implicit def arbJavaList[A](implicit arbInt: Arbitrary[A]): Arbitrary[java.util.List[A]] =
    Arbitrary(implicitly[Arbitrary[List[A]]]
      .arbitrary.map(JavaConverters.seqAsJavaList)
    )

  implicit def arbJListFoldable[A](implicit arbJList: Arbitrary[java.util.List[A]]): Arbitrary[JListFoldable[A]] =
    Arbitrary(arbJList.arbitrary.map { new JListFoldable(_) } )

  implicit def jListFoldable[A](implicit eqA: Eq[A]): Eq[JListFoldable[A]] =
    Eq.by { _.iterator.toList }


  checkAll("JListFoldable", FoldableTests[JListFoldable].foldable[Int, String])

  test("contents are as constructed")(
    forAll { li: List[Int] =>
      new JListFoldable(JavaConverters.seqAsJavaList(li)).toList should === (li)
    }
  )
}
