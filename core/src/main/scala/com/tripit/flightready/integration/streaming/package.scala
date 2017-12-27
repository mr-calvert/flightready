package com.tripit.flightready.integration

/** Abstraction inspired by contrasting FS2 with Travis Brown's
  * Iteratee.
  *
  * FlightReady aims to support not just pure FP against common IO
  * providers, but ERGONOMIC and HAPPY FP. It's clear from FS2's
  * popularity and my own experience that real world FP requires
  * streaming systems. Thus FlightReady needs to support streaming,
  * not just in as a bolt on afterthought, but support it cleanly.
  *
  * Trouble is FS2 isn't the only game in town. Travis Brown was kind
  * enough to bring his solid Iteratee project to my attention, and
  * John De Goes is making noises about writing a competitor to FS2.
  * Integrating with FS2 as a first class citizen in core FlightReady
  * would be a long term liability.
  *
  * The interface in this package is my attempt to support all
  * manner of pure FP streaming libraries via external add in modules
  * while keeping the core agnostic. This approach leverages
  * FlightReady's commitment to fine vertical decomposition of
  * algebras. (Concretely the algebra for working with, say, an open
  * file handle is different from the algebra to open a file and
  * yield a file handle.)
  *
  * This approach requires supported streaming libraries to expose a
  * stream/source type `S` with the shape `S[F[_], X]` describing a
  * program over effects in `F` producing one or more values of `X`.
  * This assumption is baked into core FlightReady effect algebras in
  * method type signatures. Operations that open a runtime resource
  * (say a file handle) itself supporting an effectful sub algebra
  * and likely requiring closure to avoid leaking resources should
  * assume the signature:
  *
  * def openFile[S, X](f: Path)
  *                    (s: FHIO[F] => S[F, X])
  *                    (implicit rs: ResourceSafety[S, F]): S[F, X]
  *
  * `openFile` will rely on [[streaming.ResourceSafety]] to combine
  * `s` with an instance of `F[FHIO[F]]` and an instance of
  * `FHIO[F] => F[Unit]` to produce an S[F, X] which safely generates
  * the FHIO instance, operates on it, and closes it with the
  * `FHIO[F] => F[Unit]`.
  */
package object streaming {

}
