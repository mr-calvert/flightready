package flightready

trait Term[IOA[_[_], _], RA[_[_], _]] {
  /** Interpret into io applying arguments and implicit knowledge
    * of required operation. */
  def selectA[F[_], A](io: IOA[F, A]): F[RA[F, A]]
}

trait TermP1[IOA[_[_], _], R] { self: Term[IOA, ({ type RFA[F[_], A] = R })#RFA] =>
  def select[F[_]](io: IOA[F, _]): F[R]
  override def selectA[F[_], A](io: IOA[F, A]) = select(io)
}

trait Term1[IOA[_[_], _], R] extends Term[IOA, ({ type RFA[F[_], A] = R })#RFA] with TermP1[IOA, R]
