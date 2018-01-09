package flightready.integration.cats

import cats.Eval
import flightready.integration.collection.{HiddenIterable, HiddenRandomAccess}

trait Foldable extends LowerPriorityRandomAccessFoldable {
  implicit def iterableFoldable[F[X] <: HiddenIterable[X]]: cats.Foldable[F] =
    new IterableFoldableImpl[F] {}
}

trait LowerPriorityRandomAccessFoldable extends LowestPriorityRandomAccessIterableFoldable {
  implicit def randomAccessFoldable[F[X] <: HiddenRandomAccess[X]]: cats.Foldable[F] =
    new RandomAccessFoldableImpl[F] {}
}

trait LowestPriorityRandomAccessIterableFoldable {
  implicit def randomAccessIterableFoldable[F[X] <: HiddenIterable[X] with HiddenRandomAccess[X]]: cats.Foldable[F] =
    new FoldableFromRandomAccessIterable[F]
}

trait IterableFoldableImpl[F[X] <: HiddenIterable[X]] extends cats.Foldable[F] {
  def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B =
    fa.iterator.foldLeft(b)(f)

  def foldRight[A, B](fa: F[A],lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
    def go(ia: Iterator[A]): Eval[B] =
      if (ia.hasNext) f(ia.next, Eval.defer(go(ia)))
      else lb

    Eval.defer(go(fa.iterator))
  }
}

trait RandomAccessFoldableImpl[F[X] <: HiddenRandomAccess[X]] extends cats.Foldable[F] {
  def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B = {
    var bb = b
    for (i <- 0 to fa.size) bb = f(bb, fa.get(i))
    bb
  }

  def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
    val sz = fa.size
    def loop(i: Int): Eval[B] =
      if (i < sz) f(fa.get(i), Eval.defer(loop(i + 1))) else lb

    Eval.defer(loop(0))
  }

  override def size[A](fa: F[A]): Long = fa.size.toLong
  override def get[A](fa: F[A])(idx: Long): Option[A] =
    if (idx > fa.size) None
    else if (idx < Int.MaxValue) Some(fa.get(idx.toInt))
    else super.get(fa)(idx)
}

class FoldableFromRandomAccessIterable[F[X] <: HiddenIterable[X] with HiddenRandomAccess[X]]
      extends RandomAccessFoldableImpl[F] with IterableFoldableImpl[F] {

  override def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B =
    super[IterableFoldableImpl].foldLeft(fa, b)(f)

  override def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
    super[IterableFoldableImpl].foldRight(fa, lb)(f)
}
