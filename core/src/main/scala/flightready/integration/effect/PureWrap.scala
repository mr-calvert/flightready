package flightready.integration.effect

import scala.language.higherKinds

/** Same signature as [[ThunkWrap]] but without the requirement to
  * delay execution or catch exceptions.
  *
  * Applicative[cats.Id].pure is a valid implementation.
  */
trait PureWrap[F[_]] {
  def apply[X](x: => X): F[X]
}
