package flightready.java.nio

import cats._
import cats.implicits._
import flightready.java.nio.file.FSPath
import flightready.test.ExampleCheck

/** Examples/compilation checks/tests for FSPath/FSPathLogic
  * functions.
  *
  * Defines functions which demonstrate the intended usage for
  * FSPath/FSPathLogic traits AND ensures that they compile.
  */
class FSPathLogicExamples extends ExampleCheck {
  /** Demonstrate abstract implementation of path arithmetic logic.
    *
    * This code searches path `p` for its longest parent path ending
    * in `name`. Thus `ancestorByName("foo/bar/baz/bat.txt", "bar")`
    * would be "foo/bar". There's no magic in what it does, it's just
    * something non trivial to demonstrate how one would write a path
    * calculation which is abstract in `F`, `P`, and `FSPath`.
    */
  def ancestorByNameOption[F[_]: Monad, P](p: P, name: P)(implicit fsp: FSPath[F, P]): F[Option[P]] = {
    def findIn(p: P): F[Option[P]] =
      fsp.endsWith(p, name).flatMap {
        if (_) Monad[F].pure(Some(p))
        else
          fsp.parentOption(p).flatMap {
            _.fold(Monad[F].pure(None: Option[P]))(findIn)
          }

      }

    fsp.normalize(p).flatMap(findIn)
  }

  test("ancestorByNameOption should work when compiled into cats.effect.IO") {
    import java.nio.file.Paths

    import cats.effect.IO

    import flightready.java.nio.file.{IsoFSPathLogic, JVMFSPathLogic}
    import flightready.integration.cats.implicits._


    // modules provide interpreters and aliases for opaque types
    val pathMod: IsoFSPathLogic.Module[IO] = JVMFSPathLogic.forDefaultFS[IO]
    val pathIso = pathMod.isoImmutableUnsafe
    implicit val fsPath = pathMod.fsPathLogic


    // since we know we're working with JVMFSPathLogic that uses real
    // instances of java.nio.file.Path behind the scenes we can lift
    // a Path we have on hand into the opaque fsMod.P type
    val p = pathIso.toOpaque(Paths.get("foo/bar/baz/bat/gobble/gobble/flow/go"))
    val ancestor =
      for {
        // FSPathLogic includes the .path method for lifting a String
        // into the opaque path type, this can fail because of
        // arbitrary rules within the JVM implementation so .path is
        // relegated to the FSPathLogic algebra not the weaker but
        // safer FSPath algebra
        name <- fsPath.path("gobble")
        ancestorOpt <- ancestorByNameOption[IO, pathMod.P](p, name)
      } yield ancestorOpt.fold("BROKEN")(pathIso.toImmutable(_).toString)

    ancestor.unsafeRunSync shouldBe "foo/bar/baz/bat/gobble/gobble"
  }
}
