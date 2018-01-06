package flightready.integration.scalaz

import scala.util.control.NonFatal

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

import flightready.integration.category.{FlatMap, Order => frOrder}
import flightready.integration.effect.{ThunkWrap, Bracket, CatchWrap, PureWrap}


object implicits extends Conversions

trait Conversions extends LowerPriorityCatchWrap {
  implicit def orderFromOrder[X](implicit ox: frOrder[X]): Order[X] =
    new Order[X] {
      def order(x: X, y: X): Ordering = Ordering.fromInt(ox.compare(x, y))
    }

  implicit def flatMapFromBind[F[_]](implicit b: Bind[F]): FlatMap[F] =
    new FlatMap[F] {
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = b.bind(fa)(f)
    }

  implicit def bracketFromMonadWithCatchable[F[_]](implicit m: Monad[F], c: Catchable[F]): Bracket[F] =
    new Bracket[F] {
      def bracket[R, A](fr: F[R])(release: R => F[Unit])(run: R => F[A]): F[A] =
        fr.flatMap { r =>
          c.attempt(run(r))
            .flatMap { ex =>
              release(r).flatMap { _ =>
                ex.fold(c.fail[A], m.pure(_))
              }
            }
        }
    }

  implicit def taskThunkWrap: ThunkWrap[Task] =
    new ThunkWrap[Task] {
      def apply[X](x: => X): Task[X] = Task.delay(x)
    }
}

trait LowerPriorityCatchWrap extends LowestPriorityPureWrap {
  implicit def catchWrapFromApplicativeWithCatchable[F[_]](implicit a: Applicative[F], c: Catchable[F]): CatchWrap[F] =
    new CatchWrap[F] {
      def apply[X](x: => X): F[X] =
        try {
          a.pure(x)
        } catch {
          case NonFatal(e) => c.fail(e)
        }
    }
}

trait LowestPriorityPureWrap {
  implicit def pureWrapFromApplicative[F[_]](implicit a: Applicative[F]): PureWrap[F] =
    new PureWrap[F] {
      def apply[X](x: => X): F[X] = a.pure(x)
    }
}

