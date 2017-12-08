package com.tripit.flightready.integration.category

/** Agnostic basis for deriving Cats/Scalaz Order instances. */
trait Order[A] {
  /** < 0 when l < r, 0 when l == r, > 0 when l > r */
  def compare(l: => A, r: => A): Int
}
