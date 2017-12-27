package flightready.java.nio.file

import scala.language.higherKinds

import java.nio.file.{StandardOpenOption, Files}

import flightready.integration.effect.{ThunkWrap, Bracket}
import flightready.integration.streaming.ResourceSafety
import flightready.java.io.{JVMInputStreamIO, InputStreamIO}
import flightready.java.nio._

object NIOFileIO {
  class Module[F[_]] extends FileIO.Module[F] {
    def fileIO: FileIO[F, NIOFileIO.Module[F]] = ???

    type P = JVMFSPathTypes#P

    type ByteBufferIOMod = NIOByteBufferModule[F]
    def byteBufferModule: ByteBufferIOMod = ???
  }
}

class NIOFileIO[F[_]](val tw: ThunkWrap[F])
      extends NIOFileReadIO[F](tw) with FileIO[F, NIOFileIO.Module[F]] with NIOFileWriteIOImpl[F] {

  override type P = NIOFileIO.Module[F]#P
  type SBCIO = SeekableByteChannelIO[F, NIOByteBufferModule[F]]

  def onByteChannelRWF[X](p: P, openOptions: OpenRWOption*)(run: SBCIO => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelRW(p, openOptions))(_.close)(run)

  def onByteChannelRWS[S[_[_], _], I, O]
                      (p: P, openOptions: OpenRWOption*)
                      (run: SeekableByteChannelIO[F, NIOByteBufferModule[F]] => S[F, I] => S[F, O])
                      (implicit rs: ResourceSafety[S, F]): S[F, O] =

    rs.bracketSink(newByteChannelRW(p, openOptions))(_.close)(run)


  private[this] def newByteChannelRW(p: P, oos: Seq[OpenRWOption]) =
    tw.wrap(
      new NIOSeekableByteChannelIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.READ +: StandardOpenOption.WRITE +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
}

class NIOFileReadIO[F[_]](tw: ThunkWrap[F]) extends FileReadIO[F, NIOFileIO.Module[F]] {
  type P = NIOFSIO.Module[F]#P

  def readAllBytes(p: P): F[Array[Byte]] = tw.wrap(Files.readAllBytes(p))

  def onInputStreamF[X](p: P)(run: InputStreamIO[F] => F[X])(implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newInputStream(p))(_.close)(run)

  def onInputStreamS[S[_[_], _], X](p: P)
                                   (s: InputStreamIO[F] => S[F,X])
                                   (implicit rs: ResourceSafety[S,F]): S[F,X] =
    rs.bracketSource(newInputStream(p))(_.close)(s)

  private[this] def newInputStream(p: P) =
    tw.wrap(new JVMInputStreamIO(Files.newInputStream(p), tw))


  type SBCReadIO = SeekableByteChannelReadIO[F, NIOByteBufferModule[F]]

  def onByteChannelROF[X](p: P, openOptions: OpenReadOption*)
                         (run: SBCReadIO => F[X])
                         (implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelRO(p, openOptions))(_.close)(run)

  def onByteChannelROS[S[_[_], _], X](p: P, openOptions: OpenReadOption*)
                                     (run: SBCReadIO => S[F, X])
                                     (implicit rs: ResourceSafety[S, F]): S[F, X] =
    rs.bracketSource(newByteChannelRO(p, openOptions))(_.close)(run)

  private[this] def newByteChannelRO(p: P, oos: Seq[OpenReadOption]) =
    tw.wrap(
      new NIOSeekableByteChannelReadIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.READ +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
}

trait NIOFileWriteIOImpl[F[_]] extends FileWriteIO[F, NIOFileIO.Module[F]] {
  type P = NIOFSIO.Module[F]#P

  def tw: ThunkWrap[F]

  def writeByteArray(f: P, content: Array[Byte]): F[P] =
    tw.wrap(
      NIOFSPathLogic.tagCheck(f, Files.createFile(f))
    )


  def onByteChannelAppendF[X](p: P, openOptions: OpenAppendOption*)
                             (run: ByteChannelWriteIO[F, NIOByteBufferModule[F]] => F[X])
                             (implicit brkt: Bracket[F]): F[X] =
    brkt.bracket(newByteChannelAppend(p, openOptions))(_.close)(run)

  def onByteChannelAppendS[S[_[_], _], I, O]
                          (p: P, openOptions: OpenAppendOption*)
                          (run: ByteChannelWriteIO[F, NIOByteBufferModule[F]] => S[F, I] => S[F, O])
                          (implicit rs: ResourceSafety[S, F]): S[F, O] =
    rs.bracketSink(newByteChannelAppend(p, openOptions))(_.close)(run)

  private[this] def newByteChannelAppend(p: P, oos: Seq[OpenAppendOption]) =
    tw.wrap(
      new NIOByteChannelWriteIO(
        Files.newByteChannel(
          p,
          StandardOpenOption.APPEND +: oos.map { _.jOO }: _*
        ),
        tw
      )
    )
}

class NIOFileWriteIO[F[_]](val tw: ThunkWrap[F]) extends NIOFileWriteIOImpl[F]
