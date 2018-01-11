package flightready

trait Term[IO[_[_]], A[_[_]]] {
  /** Interpret into io applying arguments and implicit knowledge
    * of required operation. */
  def select[F[_]](io: IO[F]): F[A[F]]
}

trait Term1[IO[_[_]], A] extends Term[IO, ({ type AF[_[_]] = A })#AF]
