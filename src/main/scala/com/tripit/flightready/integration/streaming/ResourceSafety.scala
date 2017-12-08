package com.tripit.flightready.integration.streaming

import scala.language.higherKinds

/** Captures the bracket primitive common in streaming libraries.
  *
  * Lawless for now, but eventually we'll generate laws that measure the required sequencing.
  */
trait ResourceSafety[S[_[_], _], F[_]] {
  def bracketSource[IO[_[_]], X](init: F[IO[F]])(s: IO[F] => S[F, X])(close: IO[F] => F[Unit]): S[F, X]

  def bracketSink[IO[_[_]], I, O](init: F[IO[F]])(s: IO[F] => S[F, I] => S[F, O])(close: IO[F] => F[Unit]): S[F, O]
}
