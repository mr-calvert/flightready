package com.tripit.flightready

import scala.language.higherKinds
import _root_.java.nio.file.Path

trait KV[F[_], K, V] {
  def get(k: K): F[V]
  def set(k: K, v: V): F[Unit]
}

object KV {
//  class Free[K, V] extends KV

  class JVMFSDirectory[F[_]](where: Path) extends KV[F, Path, Array[Byte]] {
    def get(file: Path): F[Array[Byte]] = ???
    def set(file: Path, contents: Array[Byte]): F[Unit] = ???
  }
}
