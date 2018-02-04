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

/** Idea: A IO[F[_], FreeA] instances are all housed in classes which close over a "target" alignment type parameter.
  * The target alignment parameter allows the actual interpreter to require a constructor value parameter which is the
  * runtime finally tagless interpreter it's being translated into. This way when free interpreters are accepted as
  * inputs to algebraic operations, that very same free interpreter's F terms can look inside the free interpreter they
  * are given and extract the relevant target interpreter.
  * 
  * This will put pressure on the implementation of algebraic terms in Free interpreters which CONSTRUCT interpreters.
  * The free version will have to somehow feed the selected target interpreter instance forward to the new Free interpreter
  * 
  */
