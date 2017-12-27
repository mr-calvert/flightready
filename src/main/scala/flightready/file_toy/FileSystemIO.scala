package flightready.file_toy

import java.nio.file.{Path, Paths}

import scala.language.{higherKinds, implicitConversions}

import com.tripit.flightready.Tagless._


object File {
  trait Module[F[_]] {
    type FH

    def fsIO: FileSystemIO[F]
    def fileIO(file: Path): FileIO[F]
    def readFileIO(fh: FH): ReadFileIO[F]
    def writeFileIO(fh: FH): WriteFileIO[F]
    def rwFileIO(fh: FH): RWFileIO[F]
  }
}

trait FileSystemIO[F[_]] {
  def list: F[List[Path]]
  def onFile[X](file: Path)(run: Run[F, X, FileIO]): F[X]
}

trait FileIO[F[_]] {
  def size: F[Long]

  def onOpenReading[X](run: Run[F, X, ReadFileIO]): F[X]
  def onOpenWriting[X](run: Run[F, X, WriteFileIO]): F[X]
  def onOpenRW[X](run: Run[F, X, RWFileIO]): F[X]
}

trait ReadFileIO[F[_]] {
  def read: F[Byte]
}

trait WriteFileIO[F[_]] {
  def put(byte: Byte): F[Unit]
  def write(s: String): F[Unit]
}

trait RWFileIO[F[_]] extends ReadFileIO[F] with WriteFileIO[F]


