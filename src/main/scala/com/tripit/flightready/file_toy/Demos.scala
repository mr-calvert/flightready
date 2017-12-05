package com.tripit.flightready.file_toy

import scala.language.higherKinds

import java.nio.file.{Paths, Path}

import cats._
import cats.implicits._

object Demos {
  def helloWorld[F[_]](p: Path)(implicit fsM: File.Module[F]): F[Unit] =
    fsM.fsIO.onFile(p) {
      _.onOpenWriting { _.write("Hi") }
    }


//  def itemCountInFile[F[_]: Monad](p: Path, sizeFile: String)(implicit fsM: File.Module[F]): F[Int] =
//    fsM.fsIO.list.flatMap { ls =>
//      val sz = ls.size
//
//      fsM.fsIO
//        .onFile(Paths.get("directory_count.txt")) {
//          _.onOpenWriting { _.write(sz.toString) }(???)
//        } >> Monad[F].pure(sz)
//    }

  // TODO: write crappy file copy to show two open files

  // TODO: demonstrate Iteratee based IO
  // TODO: demonstrate FS2 based IO
}
