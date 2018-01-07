package flightready.integration.cats

import cats._
import cats.implicits._
import cats.effect._

import flightready.integration.category.{FlatMap => frFlatMap, Order => frOrder}
import flightready.integration.effect.{PureWrap, Bracket, CatchWrap, ThunkWrap}


object implicits extends Conversions with Foldable

trait Conversions extends LowerPriorityCatchWrap {
  implicit def orderFromOrder[X](implicit ox: frOrder[X]): Order[X] =
    new Order[X] {
      def compare(x: X, y: X): Int = ox.compare(x, y)
    }

  implicit def flatMapFromFlatMap[F[_]](implicit fm: FlatMap[F]): frFlatMap[F] =
    new frFlatMap[F] {
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = fm.flatMap(fa)(f)
    }

  implicit def bracketFromMonadError[F[_]](implicit me: MonadError[F, Throwable]): Bracket[F] =
    new Bracket[F] {
      def bracket[R, A](fr: F[R])(release: R => F[Unit])(run: R => F[A]): F[A] =
        fr.flatMap { r =>
          run(r).attempt
            .flatMap { ex =>
              release(r).flatMap { _ => me.fromEither(ex) }
            }
        }
    }

  implicit def thunkWrapFromSync[F[_]](implicit s: Sync[F]): ThunkWrap[F] =
    new ThunkWrap[F] {
      def apply[X](x: => X): F[X] = s.delay(x)
    }
}

trait LowerPriorityCatchWrap extends LowestPriorityPureWrap {
  implicit def catchWrapFromApplicativeError[F[_]](implicit ae: ApplicativeError[F, Throwable]): CatchWrap[F] =
    new CatchWrap[F] {
      def apply[X](x: => X): F[X] = ae.catchNonFatal(x)
    }
}

trait LowestPriorityPureWrap {
  implicit def pureWrapFromApplicative[F[_]](implicit a: Applicative[F]): PureWrap[F] =
    new PureWrap[F] {
      def apply[X](x: => X): F[X] = a.pure(x)
    }
}
