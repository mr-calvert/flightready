package flightready.integration.effect

import scala.language.higherKinds

/** Simplified typeclass for embedding values within instances of
  * arbitrary type constructor instances.
  *
  * No formal laws yet, but ThunkWrap instances are **required** to
  * delay execution and **required** to catch and communicate
  * exceptions thrown from thunks.
  */
trait ThunkWrap[F[_]] extends PureWrap[F] {
  def apply[X](f: => X): F[X]
}
