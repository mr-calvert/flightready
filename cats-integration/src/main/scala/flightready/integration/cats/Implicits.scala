package flightready.integration.cats

import cats._

import flightready.integration.category.{Order => frOrder}

object Implicits {
  implicit def orderFromOrder[X](implicit ox: frOrder[X]): Order[X] =
    new Order[X] {
      def compare(x: X, y: X): Int = ox.compare(x, y)
    }
}
