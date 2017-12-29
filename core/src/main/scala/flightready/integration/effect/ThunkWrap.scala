package flightready.integration.effect

/** Same signature as [[ThunkWrap]] but without the requirement to
  * delay execution or catch exceptions.
  *
  * Applicative[Id].pure is a valid implementation.
  */
trait PureWrap[F[_]] {
  def apply[X](f: => X): F[X]
}

/** [[PureWrap]] adding the requirement that exceptions are caught
  * and communicated.
  */
trait CatchWrap[F[_]] extends PureWrap[F] {
  def apply[X](f: => X): F[X]
}

/** Typeclass for embedding values within instances of arbitrary type
  * constructor instances.
  *
  * No formal laws yet, but ThunkWrap instances are **required** to
  * delay execution and **required** to catch and communicate
  * exceptions thrown from thunks.
  */
trait ThunkWrap[F[_]] extends CatchWrap[F] {
  def apply[X](f: => X): F[X]
}
