package flightready.integration.effect


/** Typeclass for embedding bracket combinator resource safety. */
trait Bracket[F[_]] {
  def bracket[R, A](fr: F[R])(release: R => F[Unit])(run: R => F[A]): F[A]
}
