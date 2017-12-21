package com.tripit.flightready.integration.effect

import cats.Applicative

import scala.language.higherKinds
import cats.effect.Sync

/** Simplified typeclass for embedding values within instances of
  * arbitrary type constructor instances.
  *
  * It's fundamentally lawless, but it admits implementing plausibly
  * effectful algebras by deference to external libraries which
  * happen to be pure. Of course one could duplicate an algebra's
  * implementation across multiple non [[cats.effect.Sync]] types,
  * but that's a lot of work.
  */
trait ThunkWrap[F[_]] {
  def wrap[X](f: => X): F[X]
}

object ThunkWrap {
  def apply[F[_]](implicit F: ThunkWrap[F]): ThunkWrap[F] = F

  /** As pure thunks are rare creatures we're avoiding making this
    * one implicit. You gotta ask for this behavior. */
  def intoPure[F[_]: Applicative]: ThunkWrap[F] =
    new ThunkWrap[F] {
      def wrap[X](f: => X): F[X] =
        Applicative[F].pure(f)
    }

  implicit def DelayInSync[F[_]: Sync]: ThunkWrap[F] =
    new ThunkWrap[F] {
      def wrap[X](f: => X): F[X] =
        Sync[F].delay(f)
    }
}
