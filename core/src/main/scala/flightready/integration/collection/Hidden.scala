package flightready.integration.collection

/** Supporting creation of cats/scalaz Foldable instances.
  *
  * Got a weird Java collection like structure you want to expose as
  * a Foldable? Box it in something extending [[HiddenIterable]] or
  * [[HiddenRandomAccess]] and let the Foldable instances in cats
  * and scalaz integration do their thing.
  */
trait HiddenIterable[A] {
  private[integration] def iterator: Iterator[A]
}

trait HiddenRandomAccess[A] {
  private[integration] def size: Int
  private[integration] def get(idx: Int): A
}
