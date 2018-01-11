package flightready

trait Lift[IO[_[_]], F[_]] {
  def apply[IO2[X[_]] >: IO[X], A](rf: Term[IO2, A]): F[A]
}
