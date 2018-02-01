package flightready

trait Term[IOA[_[_], _], RA[_[_], _]] {
  /** Interpret into io applying arguments and implicit knowledge
    * of required operation. */
  def selectA[F[_], A](io: IOA[F, A]): F[RA[F, A]]
}

trait Term1[IO[_[_], _], R] extends Term[IO, ({ type RFA[F[_], A] = R })#RFA] {
  def select[F[_], A](io: IO[F, A]): F[R]
  override def selectA[F[_], A](io: IO[F, A]) = select(io)
}
