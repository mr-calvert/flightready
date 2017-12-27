package com.tripit.flightready.util

object TaggedNewtype {
  // yeah, this is totally cribbed from Scalaz
  type Tagged[T] = { type Tag = T }
  type @@[+T, Tag] = T with Tagged[Tag]

  object Tag {
    @inline
    def apply[A, T](a: A): A @@ T =
      a.asInstanceOf[A @@ T]
  }
}
