package com.tripit.flightready

import scala.language.higherKinds

/** Isomorphism between IO types and underlying immutable but unsafe
  * resources.
  *
  * `O` is an opaque type representing the operand type of an IO
  * algebra. `X` is an external to FlightReady immutable type which,
  * immutability notwithstanding, does support unsafe operations.
  *
  * This typeclass exists to provide a portal between the pure world
  * of FlightReady to systems which need direct and unsafe access to
  * the target type.
  */
trait IsoImmutableUnsafe[O, X] {
  def toOpaque(x: X): O
  def toImmutable(o: O): X
}
