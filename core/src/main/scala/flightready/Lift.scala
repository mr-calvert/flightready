package flightready

trait Lift[IO[_[_]], F[_]] {
  def apply[A](rf: Term[IO, ({ type AF[_[_]] = A})#AF]): F[A]
  def fbound[A[_[_]]](rf: Term[IO, A]): F[A[F]]
}
