package flightready.integration.effect

import scala.language.higherKinds

/** Simplified typeclass for embedding values within instances of
  * arbitrary type constructor instances.
  */
trait ThunkWrap[F[_]] {
  def wrap[X](f: => X): F[X]
}

object ThunkWrap {
  def apply[F[_]](implicit F: ThunkWrap[F]): ThunkWrap[F] = F
}
