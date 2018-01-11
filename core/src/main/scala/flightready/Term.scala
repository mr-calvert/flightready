package flightready

trait Term[IO[_[_]], A] {
  /** Interpret into io applying arguments and implicit knowledge
    * of required operation. */
  def select[F[_]](io: IO[F]): F[A]
}

trait Term2[IO[_[_]], A[_[_]]] {
  /** Interpret into io applying arguments and implicit knowledge
    * of required operation. */
  def select[F[_]](io: IO[F]): F[A[F]]
}

trait Term21[IO[_[_]], A] extends Term2[IO, ({ type AF[_[_]] = A })#AF]
