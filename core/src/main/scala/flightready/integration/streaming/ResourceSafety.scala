package flightready.integration.streaming


/** Captures the bracket primitive common in streaming libraries.
  *
  * Lawless for now, but eventually we'll generate laws that measure the required sequencing.
  */
trait ResourceSafety[S[_[_], _], F[_]] {
  def bracketSource[IO[_[_]], X](init: F[IO[F]])(close: IO[F] => F[Unit])(s: IO[F] => S[F, X]): S[F, X]

  def bracketSink[IO[_[_]], I, O](init: F[IO[F]])(close: IO[F] => F[Unit])(s: IO[F] => S[F, I] => S[F, O]): S[F, O]
}
