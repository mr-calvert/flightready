package flightready.integration.collection

import scala.collection.JavaConverters

class JListFoldable[A](l: java.util.List[A]) extends HiddenIterable[A] with HiddenRandomAccess[A] {
  private[integration] def iterator: Iterator[A] = JavaConverters.asScalaIterator(l.iterator)
  private[integration] def size = l.size
  private[integration] def get(idx: Int) = l.get(idx)
}
