package com.tripit.flightready

import scala.language.higherKinds

/** Isomorphism between IO types and underlying mutable resources.
  *
  * `IO` is a resource tied algebra type, R is an external to
  * FlightReady mutable type representing some resource.
  *
  * Where possible mutable resources should be created using the pure
  * effect modeling provided by various FlightReady interfaces.
  * [[com.tripit.flightready.java.nio.file.ByteBufferIO.ModuleIso]]
  * provides an example of effectful creation in its `allocate`
  * methods.
  *
  * FlightReady is intended to facilitate pure FP programming against
  * tools and services written without FP in mind. For this goal FP's
  * edge, the "end of the world," needs to be flexibly and
  * thoughtfully designed.
  *
  * This (potentially unlawful) typeclass is critical to
  * FlightReady's modelling of the end of the world. This typeclass
  * supports moving active mutable resources into and out of
  * FlightReady's pure FP world. This will be relevant:
  *  * Migrating large impure codebases towards purity
  *  * Pure creation of resources which are fed to impure systems.
  *  * Pure manipulation of resources created by impure systems.
  *
  * The purity of this typeclass rests on the `R` object instances
  * supporting a transitive equality operation regarding underlying
  * resource. It's a bit of a whispy notion, but we ARE at the edge of
  * the world.
  */
trait IsoMutable[IO[_[_]], F[_], R] {
  def toIO(r: R): IO[F]
  def toMutable(io: IO[F]): R
}
