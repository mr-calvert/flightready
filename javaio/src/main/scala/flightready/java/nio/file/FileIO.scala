package flightready.java.nio.file

import java.nio.file.StandardOpenOption

import flightready.integration.collection.JListFoldable
import flightready.integration.effect.Bracket
import flightready.integration.streaming.ResourceSafety
import flightready.java.io.InputStreamIO
import flightready.java.nio.{ByteBufferIO, ByteChannelWriteIO, SeekableByteChannelIO, SeekableByteChannelReadIO}

object FileIO {
  trait Module[F[_]] {
    def fileIO: FileIO[F, _ <: FileIO.Module[F]]

    type P

    type ByteBufferIOMod <: ByteBufferIO.Module[F]
    def byteBufferModule: ByteBufferIOMod

    type CS
  }
}

trait FileIO[F[_], Mod <: FileIO.Module[F]] extends FileReadIO[F, Mod] with FileWriteIO[F, Mod] {
  def onByteChannelRWF[X](p: Mod#P, openOptions: OpenRWOption*)
                         (run: SeekableByteChannelIO[F, Mod#ByteBufferIOMod] => F[X])
                         (implicit brkt: Bracket[F]): F[X]
  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: Mod#P, openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, Mod#ByteBufferIOMod] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O]
}

trait FileReadIO[F[_], Mod <: FileIO.Module[F]] {
  def readAllBytes(p: Mod#P): F[Array[Byte]]
  def readAllLines(p: Mod#P, cs: Mod#CS): F[JListFoldable[String]]

  // TODO: add OpenOptions parameter
  def onInputStreamF[X](p: Mod#P)(run: InputStreamIO[F] => F[X])(implicit brkt: Bracket[F]): F[X]
  def onInputStreamS[S[_[_], _], X](p: Mod#P)
                                   (s: InputStreamIO[F] => S[F, X])
                                   (implicit rs: ResourceSafety[S, F]): S[F, X]

  def onByteChannelROF[X](p: Mod#P, openOption: OpenReadOption*)
                         (run: SeekableByteChannelReadIO[F, Mod#ByteBufferIOMod] => F[X])
                         (implicit brkt: Bracket[F]): F[X]

  def onByteChannelROS[S[_[_], _], X](p: Mod#P, openOption: OpenReadOption*)
                                     (run: SeekableByteChannelReadIO[F, Mod#ByteBufferIOMod] => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X]
}

trait FileWriteIO[F[_], Mod <: FileIO.Module[F]] {
  def writeByteArray(p: Mod#P, content: Array[Byte]): F[Mod#P] // TODO: add OpenOptions parameter

  // TODO: taunt anybody who wants to use size or truncate in append mode and tell them to PR it
  def onByteChannelAppendF[X](p: Mod#P, openOptions: OpenAppendOption*)
                             (run: ByteChannelWriteIO[F, Mod#ByteBufferIOMod] => F[X])
                             (implicit brkt: Bracket[F]): F[X]
  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: Mod#P, openOptions: OpenAppendOption*)
                          (run: ByteChannelWriteIO[F, Mod#ByteBufferIOMod] => S[F, I] => S[F, O])
                          (implicit rs: ResourceSafety[S, F]): S[F, O]
}

sealed trait OpenOption {
  def jOO: java.nio.file.OpenOption
}

sealed trait OpenReadOption extends OpenOption
object OpenReadOption {
  case object DeleteOnClose extends OpenReadOption { def jOO = StandardOpenOption.DELETE_ON_CLOSE }
}

sealed trait OpenAppendOption extends OpenOption
object OpenAppendOption {
  case object Create extends OpenAppendOption { def jOO = StandardOpenOption.CREATE }
  case object DeleteOnClose extends OpenAppendOption { def jOO = StandardOpenOption.DELETE_ON_CLOSE }
  case object Sparse extends OpenAppendOption { def jOO = StandardOpenOption.SPARSE }
  case object Sync extends OpenAppendOption { def jOO = StandardOpenOption.SYNC }
  case object DSync extends OpenAppendOption { def jOO = StandardOpenOption.DSYNC }
}

sealed trait OpenRWOption extends OpenOption
object OpenRWOption {
  case object TruncateExisting extends OpenRWOption { def jOO = StandardOpenOption.TRUNCATE_EXISTING }
  case object CreateNew extends OpenRWOption { def jOO = StandardOpenOption.CREATE_NEW }
  case object Create extends OpenRWOption { def jOO = StandardOpenOption.CREATE }
  case object DeleteOnClose extends OpenRWOption { def jOO = StandardOpenOption.DELETE_ON_CLOSE }
  case object Sparse extends OpenRWOption { def jOO = StandardOpenOption.SPARSE }
  case object Sync extends OpenRWOption { def jOO = StandardOpenOption.SYNC }
  case object DSync extends OpenRWOption { def jOO = StandardOpenOption.DSYNC }
}