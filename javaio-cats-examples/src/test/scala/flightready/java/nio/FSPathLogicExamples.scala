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
  type P[A, D] = FSPath.P[A, D]

  /** Demonstrate abstract implementation of path arithmetic logic.
    *
    * This code searches path `p` for its longest parent path ending
    * in `name`. Thus `ancestorByName("foo/bar/baz/bat.txt", "bar")`
    * would be "foo/bar". There's no magic in what it does, it's just
    * something non trivial to demonstrate how one would write a path
    * calculation which is abstract in `F`, `P`, and `FSPath`.
    */
  def ancestorByNameOption[F[_]: Monad, A, D](p: P[A, D], name: P[A, D])(implicit fsp: FSPath[F, A]): 
      F[Option[P[A, D]]] = {

    def findIn(p: P[A, D]): F[Option[P[A, D]]] =
      fsp.endsWith(p, name).flatMap {
        if (_) Monad[F].pure(Some(p))
        else
          fsp.parentOption(p).flatMap {
            _.fold(Monad[F].pure(None: Option[P[A, D]]))(findIn)
          }

      }

    fsp.normalize(p).flatMap(findIn)
  }

  test("ancestorByNameOption should work when compiled into cats.effect.IO") {
    import java.nio.file.Paths

    import cats.effect.IO

    import flightready.integration.cats.implicits._
    import flightready.java.JVMA
    import flightready.java.nio.file.JVMFSPath


    import JVMFSPath.jvmFSPath

    trait StandinDomain
    type D = StandinDomain

    val pathIso = JVMFSPath.isoPath[D]

    // since we know we're working with JVMFSPathLogic that uses real
    // instances of java.nio.file.Path behind the scenes we can lift
    // a Path we have on hand into the opaque FSPath.P[JVMA, D] type
    // with a cheap fake D domaim type
    val p = pathIso.toOpaque(Paths.get("foo/bar/baz/bat/gobble/gobble/flow/go"))
    val name = pathIso.toOpaque(Paths.get("gobble")) // TODO: make this with the DefaultFileSystem algebra
    val ancestor =
      for {
        ancestorOpt <- ancestorByNameOption[IO, JVMA, D](p, name)
      } yield ancestorOpt.fold("BROKEN")(pathIso.toImmutable(_).toString)

    ancestor.unsafeRunSync shouldBe "foo/bar/baz/bat/gobble/gobble"
  }

  test("ancestorByNameOption should work when compiled into cats.Id") {
    import java.nio.file.Paths

    import flightready.integration.cats.implicits._
    import flightready.java.JVMA
    import flightready.java.nio.file.JVMFSPath


    import JVMFSPath.jvmFSPath

    trait StandinDomain
    type D = StandinDomain

    val pathIso = JVMFSPath.isoPath[D]
    val fsPath = implicitly[FSPath[Id, JVMA]]

    val p = pathIso.toOpaque(Paths.get("up/under/over/around/though"))
    val name = pathIso.toOpaque(Paths.get("under/over"))

    ancestorByNameOption[Id, JVMA, D](p, name)
      .map(fsPath.string)
      .value shouldBe "up/under/over"
  }
}
