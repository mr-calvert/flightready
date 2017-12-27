package flightready.integration.category

import scala.language.higherKinds

trait FlatMap[F[_]] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
