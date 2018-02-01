package flightready

/** Knows how to put a bare algebra term into a particular F.
  * 
  * F may be as simple as Free on IO, or some complex optimized coproduct representation.
  * Maybe even could be lifted into something from Eff.
  */
trait Lift[IO[_[_], _], F[_]] {
  def apply[R](rf: Term[IO, ({ type RF[X[_], A] = R })#RF]): F[R]
  def fbound[Q[_[_], _], A](rf: Term[IO, Q]): F[Q[F, A]]
}
