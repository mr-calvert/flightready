package flightready.java.nio

import cats._
import cats.implicits._

import flightready.java.nio.file.FSPath

/** Examples/compilation checks/tests for FSPath/FSPathLogic
  * functions.
  *
  * Defines functions which demonstrate the intended usage for
  * FSPath/FSPathLogic traits AND ensures that they compile.
  */
object FSPathLogicExamples {
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
}
