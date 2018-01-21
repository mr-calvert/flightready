package flightready

trait Term[IOA[_[_], _], RA[_[_], _]] {
  /** Interpret into io applying arguments and implicit knowledge
    * of required operation. */
  def selectA[F[_], A](io: IOA[F, A]): F[RA[F, A]]
}

trait Term1[IO[_[_]], R]
    extends Term[({ type IOA[F[_], A] = IO[F] })#IOA,
                 ({ type RFA[F[_], A] = R })#RFA] {

  def select[F[_]](io: IO[F]): F[R]
  override def selectA[F[_], A](io: IO[F]) = select(io)
}
