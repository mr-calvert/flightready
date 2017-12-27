package flightready

import scala.language.higherKinds

/** Isomorphism between IO types and underlying mutable resources.
  *
  * `IO` is a resource tied algebra type, R is an external to
  * FlightReady mutable type representing some resource.
  *
  * Where possible mutable resources should be created using the pure
  * effect modeling provided by various FlightReady interfaces.
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
trait IsoMutable[IO, R] {
  def toIO(r: R): IO
  def toMutable(io: IO): R
}

/** [[IsoMutable]] modified to support `R` being optionally read
  * only.
  *
  * Requires a read only and read/write version of the algebra. `R`
  * is assumed to be marked as read only with a value level flag.
  * Thus any `R` may be converted to an `IORO` but only `R` values
  * without the read only flag may be converted to `IORW`. As the
  * flag is hidden at type level the conversion to IORW must be
  * partial.
  */
trait IsoMutableRORW[IORO, IORW, R] {
  def toIORO(r: R): IORO
  def toIORW(r: R): Option[IORW]
  def toMutable(io: IORO): R
}
