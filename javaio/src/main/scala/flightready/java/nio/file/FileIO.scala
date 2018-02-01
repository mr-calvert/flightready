package flightready.java.nio.file

import java.nio.file.StandardOpenOption

import flightready.integration.effect.Bracket
import flightready.integration.streaming.ResourceSafety
import flightready.java.io.InputStreamIO
import flightready.java.nio.{ ByteChannelWriteIO, SeekableByteChannelIO, SeekableByteChannelReadIO }

object FileIO {
  trait Module[F[_]] {
    def fileIO: FileIO[F, _ <: FileIO.Module[F]]
  }
}

trait FileIO[F[_], A] extends FileReadIO[F, A] with FileWriteIO[F, A] {
  override type P[D] = FSPath.P[A, D]

  def onByteChannelRWF[X](p: P[_], openOptions: OpenRWOption*)
                         (run: SeekableByteChannelIO[F, A] => F[X])
                         (implicit brkt: Bracket[F]): F[X]

  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: P[_], openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, A] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O]
}

trait FileReadIO[F[_], A] {
  type P[D] = FSPath.P[A, D]

  def readAllBytes(p: P[_]): F[Array[Byte]]

  // TODO: add OpenOptions parameter
  def onInputStreamF[X](p: P[_])(run: InputStreamIO[F, A] => F[X])(implicit brkt: Bracket[F]): F[X]
  def onInputStreamS[S[_[_], _], X](p: P[_])
                                   (s: InputStreamIO[F, A] => S[F, X])
                                   (implicit rs: ResourceSafety[S, F]): S[F, X]

  def onByteChannelROF[X](p: P[_], openOption: OpenReadOption*)
                         (run: SeekableByteChannelReadIO[F, A] => F[X])
                         (implicit brkt: Bracket[F]): F[X]

  def onByteChannelROS[S[_[_], _], X](p: P[_], openOption: OpenReadOption*)
                                     (run: SeekableByteChannelReadIO[F, A] => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X]
}

trait FileWriteIO[F[_], A] {
  type P[D] = FSPath.P[A, D]

  def writeByteArray[D](p: P[D], content: Array[Byte]): F[P[D]] // TODO: add OpenOptions parameter

  // TODO: taunt anybody who wants to use size or truncate in append mode and tell them to PR it
  def onByteChannelAppendF[X](p: P[_], openOptions: OpenAppendOption*)
                             (run: ByteChannelWriteIO[F, A] => F[X])
                             (implicit brkt: Bracket[F]): F[X]
  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: P[_], openOptions: OpenAppendOption*)
                          (run: ByteChannelWriteIO[F, A] => S[F, I] => S[F, O])
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
