package flightready

trait Lift[IO[_[_], _], F[_]] {
  def apply[R](rf: Term[IO, ({ type RF[X[_], A] = R })#RF]): F[R]
  def fbound[Q[_[_], _], A](rf: Term[IO, Q]): F[Q[F, A]]
}

trait Lift1[IO[_[_]], F[_]] extends Lift[({ type IOA[X[_], _] = IO[X] })#IOA, F]
