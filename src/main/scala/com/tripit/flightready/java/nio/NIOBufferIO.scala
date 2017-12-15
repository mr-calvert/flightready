package com.tripit.flightready.java.nio

import scala.language.higherKinds
import scala.reflect.{ClassTag, classTag}

import java.nio.{Buffer, DoubleBuffer, ByteOrder}

import com.tripit.flightready.{ThunkWrap, IsoMutableRORW}
import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.java.nio.file.PutBufferFallback

object NIOBufferIO {

}

trait NIOBufferReadIO[F[_], A] extends BufferReadIO[F, A] {
  def buf: Buffer
  def tw: ThunkWrap[F]

  def isDirect: F[Boolean] = tw.wrap(buf.isDirect)

  def capacity: F[Int] = tw.wrap(buf.capacity)
  def limit: F[Int] = tw.wrap(buf.limit)
  def mark: F[Unit] = tw.wrap(buf.mark)
  def position: F[Int] = tw.wrap(buf.position)
  def hasRemaining: F[Boolean] = tw.wrap(buf.hasRemaining)
  def remaining: F[Int] = tw.wrap(buf.remaining)

  def setLimit(limit: Int): F[Unit] = tw.wrap(buf.limit(limit))
  def setPosition(position: Int): F[Unit] = tw.wrap(buf.position(position))

  def clear: F[Unit] = tw.wrap(buf.clear)
  def flip: F[Unit] = tw.wrap(buf.flip)
  def reset: F[Unit] = tw.wrap(buf.reset)
  def rewind: F[Unit] = tw.wrap(buf.rewind)
}

trait NIOBufferIO[F[_], A] extends BufferIO[F, A] {
  def buf: Buffer
  def tw: ThunkWrap[F]

  def hasArray: F[Boolean] = tw.wrap(buf.hasArray)
  def arrayOffset: F[Int] = tw.wrap(buf.arrayOffset)
}

object NIODoubleBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoDoubleBufferIO.Module[F] =
    new IsoDoubleBufferIO.Module[F] {
      def allocate(capacity: Int): F[DoubleBufferIO[F]] =
        tw.wrap(new NIODoubleBufferIO[F](DoubleBuffer.allocate(capacity), tw))

      def wrap(doubles: Array[Double]): DoubleBufferIO[F] =
        new NIODoubleBufferIO[F](DoubleBuffer.wrap(doubles), tw)
      def wrap(doubles: Array[Double], ofs: Int, len: Int): DoubleBufferIO[F] =
        new NIODoubleBufferIO[F](DoubleBuffer.wrap(doubles, ofs, len), tw)

      type IORO = NIODoubleBufferReadIO[F]
      type IORW = NIODoubleBufferIO[F]

      def isoMutable: IsoMutableRORW[IORO, IORW, DoubleBuffer] =
        new IsoMutableRORW[IORO, IORW, DoubleBuffer] {
          def toMutable(io: NIODoubleBufferReadIO[F]): DoubleBuffer = io.buf
          def toIORO(db: DoubleBuffer): NIODoubleBufferReadIO[F] = new NIODoubleBufferReadIO(db, tw)
          def toIORW(db: DoubleBuffer): Option[NIODoubleBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new NIODoubleBufferIO(db, tw))
        }

    }
}

class NIODoubleBufferReadIO[F[_]](val buf: DoubleBuffer, val tw: ThunkWrap[F])
    extends NIOBufferReadIO[F, Double] with DoubleBufferReadIO[F]  {

  def duplicateRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Double] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Double] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Double]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Double], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIODoubleBufferIO[F[_]](buf: DoubleBuffer, tw: ThunkWrap[F])
    extends NIODoubleBufferReadIO[F](buf, tw)
      with NIOBufferIO[F, Double]
      with DoubleBufferIO[F] {

  override def duplicateRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[DoubleBufferIO[F]] = tw.wrap(new NIODoubleBufferIO(buf.duplicate, tw))
  def sliceRW: F[DoubleBufferIO[F]] = tw.wrap(new NIODoubleBufferIO(buf.slice, tw))

  def array: F[Array[Double]] = tw.wrap(buf.array)

  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(d: Double): F[Unit] = tw.wrap(buf.put(d))
  def putArray(ds: Array[Double]): F[Unit] = tw.wrap(buf.put(ds))
  def putArraySlice(ds: Array[Double], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(ds, ofs, len))
  def putAt(idx: Int, d: Double): F[Unit] = tw.wrap(buf.put(idx, d))

  def putBuffer(in: DoubleBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Double](this, tw, fm)
    in match {
      case nio: NIODoubleBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Double]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: DoubleBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}