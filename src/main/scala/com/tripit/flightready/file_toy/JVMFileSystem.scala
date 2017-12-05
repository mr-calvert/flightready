package com.tripit.flightready.file_toy

import scala.language.higherKinds
import java.nio.file.{Path, Paths}
import java.io.RandomAccessFile

import cats._
import cats.implicits._
import cats.effect.Sync
import com.tripit.flightready.Tagless.Run

object JVMFileSystem {
  implicit def module[F[_]: Sync]: File.Module[F] =
    new File.Module[F] {
      type FH = RandomAccessFile

      def fsIO =
        new FileSystemIO[F] {
          def list: F[List[Path]] = ??? // Paths.get("").toFile.list()
          def onFile[X](file: Path)(run: Run[F, X, FileIO]): F[X] =
            run(fileIO(file))
        }

      def fileIO(file: Path): FileIO[F] =
        new FileIO[F] {
          def size: F[Long] = ???

          def onOpenReading[X](run: Run[F, X, ReadFileIO]): F[X] = onOpen("r", run, readFileIO)
          def onOpenWriting[X](run: Run[F, X, WriteFileIO]): F[X] = onOpen("rw", run, writeFileIO)
          def onOpenRW[X](run: Run[F, X, RWFileIO]): F[X] = onOpen("rw", run, rwFileIO)

          def onOpen[X, Alg[_[_]]](mode: String, run: Run[F, X, Alg], mkIO: FH => Alg[F]): F[X] =
            Sync[F].delay { new RandomAccessFile(file.toString, "rw") }
              .flatMap {
                raf =>
                  for {
                    x <- run(mkIO(raf)).attempt
                    _ <- Sync[F].delay { raf.close() }
                  } yield x
              }
              .flatMap { Sync[F].fromEither }
        }

      def readFileIO(raf: FH): ReadFileIO[F] = rwFileIO(raf)
      def writeFileIO(raf: FH): WriteFileIO[F] = rwFileIO(raf)

      def rwFileIO(raf: FH): RWFileIO[F] =
        new RWFileIO[F] {
          def read: F[Byte] = ???
          def put(byte: Byte): F[Unit] = ???
          def write(s: String): F[Unit] = ???
        }
    }
}
