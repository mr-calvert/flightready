package com.tripit.flightready.java.nio

import scala.language.higherKinds

import java.nio._

import com.tripit.flightready.{ThunkWrap, IsoMutableRORW}
import com.tripit.flightready.integration.category.FlatMap
import com.tripit.flightready.java.nio.file.PutBufferFallback


trait BufferAndWrap[F[_]] {
  def buf: Buffer
  def tw: ThunkWrap[F]
}

trait NIOBufferReadIO[F[_], A] extends BufferReadIO[F, A] { self: BufferAndWrap[F] =>
  def isDirect: F[Boolean] = tw.wrap(buf.isDirect)
  def capacity: Int = buf.capacity

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

trait NIOBufferIO[F[_], A] extends BufferIO[F, A] { self: BufferAndWrap[F] =>
  def hasArray: Boolean = buf.hasArray
  def arrayOffset: Int = buf.arrayOffset
}


object NIOByteBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoByteBufferIO.Module[F] =
    new IsoByteBufferIO.Module[F] {
      def allocate(capacity: Int): F[ByteBufferIO[F]] =
        tw.wrap(new NIOByteBufferIO[F](ByteBuffer.allocate(capacity), tw))

      def allocateDirect(capacity: Int): F[ByteBufferIO[F]] =
        tw.wrap(new NIOByteBufferIO[F](ByteBuffer.allocateDirect(capacity), tw))

      def wrapArray(shorts: Array[Byte]): F[ByteBufferIO[F]] =
        tw.wrap(new NIOByteBufferIO[F](ByteBuffer.wrap(shorts), tw))
      def wrapArraySlice(shorts: Array[Byte], ofs: Int, len: Int): F[ByteBufferIO[F]] =
        tw.wrap(new NIOByteBufferIO[F](ByteBuffer.wrap(shorts, ofs, len), tw))

      type IORO = NIOByteBufferReadIO[F]
      type IORW = NIOByteBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, ByteBuffer] =
        new IsoMutableRORW[IORO, IORW, ByteBuffer] {
          def toMutable(io: NIOByteBufferReadIO[F]): ByteBuffer = io.buf
          def toIORO(sb: ByteBuffer): NIOByteBufferReadIO[F] = new NIOByteBufferReadIO(sb, tw)
          def toIORW(sb: ByteBuffer): Option[NIOByteBufferIO[F]] =
            if (sb.isReadOnly) None
            else Some(new NIOByteBufferIO(sb, tw))
        }

    }
}

class NIOByteBufferReadIO[F[_]](val buf: ByteBuffer, val tw: ThunkWrap[F])
  extends NIOBufferReadIO[F, Byte] with ByteBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[ByteBufferReadIO[F]] = tw.wrap(new NIOByteBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[ByteBufferReadIO[F]] = tw.wrap(new NIOByteBufferReadIO[F](buf.slice, tw))

  def asCharBufferRO: F[CharBufferReadIO[F]] = tw.wrap(new NIOCharBufferReadIO[F](buf.asCharBuffer, tw))
  def asShortBufferRO: F[ShortBufferReadIO[F]] = tw.wrap(new NIOShortBufferReadIO[F](buf.asShortBuffer, tw))
  def asIntBufferRO: F[IntBufferReadIO[F]] = tw.wrap(new NIOIntBufferReadIO[F](buf.asIntBuffer, tw))
  def asLongBufferRO: F[LongBufferReadIO[F]] = tw.wrap(new NIOLongBufferReadIO[F](buf.asLongBuffer, tw))
  def asFloatBufferRO: F[FloatBufferReadIO[F]] = tw.wrap(new NIOFloatBufferReadIO[F](buf.asFloatBuffer, tw))
  def asDoubleBufferRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO[F](buf.asDoubleBuffer, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Byte] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Byte] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Byte]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Byte], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIOByteBufferIO[F[_]](buf: ByteBuffer, tw: ThunkWrap[F])
  extends NIOByteBufferReadIO[F](buf, tw) with NIOBufferIO[F, Byte] with ByteBufferIO[F] {

  override def duplicateRO: F[ByteBufferReadIO[F]] = tw.wrap(new NIOByteBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[ByteBufferReadIO[F]] = tw.wrap(new NIOByteBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[ByteBufferIO[F]] = tw.wrap(new NIOByteBufferIO(buf.duplicate, tw))
  def sliceRW: F[ByteBufferIO[F]] = tw.wrap(new NIOByteBufferIO(buf.slice, tw))

  def setByteOrder(bo: ByteOrder): F[Unit] = tw.wrap(buf.order(bo))

  override def asCharBufferRO: F[CharBufferReadIO[F]] =
    tw.wrap(new NIOCharBufferReadIO[F](buf.asReadOnlyBuffer.asCharBuffer, tw))
  override def asShortBufferRO: F[ShortBufferReadIO[F]] =
    tw.wrap(new NIOShortBufferReadIO[F](buf.asReadOnlyBuffer.asShortBuffer, tw))
  override def asIntBufferRO: F[IntBufferReadIO[F]] =
    tw.wrap(new NIOIntBufferReadIO[F](buf.asReadOnlyBuffer.asIntBuffer, tw))
  override def asLongBufferRO: F[LongBufferReadIO[F]] =
    tw.wrap(new NIOLongBufferReadIO[F](buf.asReadOnlyBuffer.asLongBuffer, tw))
  override def asFloatBufferRO: F[FloatBufferReadIO[F]] =
    tw.wrap(new NIOFloatBufferReadIO[F](buf.asReadOnlyBuffer.asFloatBuffer, tw))
  override def asDoubleBufferRO: F[DoubleBufferReadIO[F]] =
    tw.wrap(new NIODoubleBufferReadIO[F](buf.asReadOnlyBuffer.asDoubleBuffer, tw))

  def asCharBufferRW: F[CharBufferIO[F]] = tw.wrap(new NIOCharBufferIO[F](buf.asCharBuffer, tw))
  def asShortBufferRW: F[ShortBufferIO[F]] = tw.wrap(new NIOShortBufferIO[F](buf.asShortBuffer, tw))
  def asIntBufferRW: F[IntBufferIO[F]] = tw.wrap(new NIOIntBufferIO[F](buf.asIntBuffer, tw))
  def asLongBufferRW: F[LongBufferIO[F]] = tw.wrap(new NIOLongBufferIO[F](buf.asLongBuffer, tw))
  def asFloatBufferRW: F[FloatBufferIO[F]] = tw.wrap(new NIOFloatBufferIO[F](buf.asFloatBuffer, tw))
  def asDoubleBufferRW: F[DoubleBufferIO[F]] = tw.wrap(new NIODoubleBufferIO[F](buf.asDoubleBuffer, tw))

  def array: Array[Byte] = buf.array
  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(f: Byte): F[Unit] = tw.wrap(buf.put(f))
  def putArray(fs: Array[Byte]): F[Unit] = tw.wrap(buf.put(fs))
  def putArraySlice(ss: Array[Byte], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(ss, ofs, len))
  def putAt(idx: Int, s: Byte): F[Unit] = tw.wrap(buf.put(idx, s))

  def putBuffer(in: ByteBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Byte](this, tw, fm)
    in match {
      case nio: NIOByteBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Byte]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: ByteBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }

  def putChar(c: Char): F[Unit] = tw.wrap(buf.putChar(c))
  def putCharAt(idx: Int, c: Char): F[Unit] = tw.wrap(buf.putChar(idx, c))
  def putShort(s: Short): F[Unit] = tw.wrap(buf.putShort(s))
  def putShortAt(idx: Int, s: Short): F[Unit] = tw.wrap(buf.putShort(idx, s))
  def putInt(i: Int): F[Unit] = tw.wrap(buf.putInt(i))
  def putIntAt(idx: Int, i: Int): F[Unit] = tw.wrap(buf.putInt(i, idx))
  def putLong(l: Long): F[Unit] = tw.wrap(buf.putLong(l))
  def putLongAt(idx: Int, l: Long): F[Unit] = tw.wrap(buf.putLong(idx, l))
  def putFloat(f: Float): F[Unit] = tw.wrap(buf.putFloat(f))
  def putFloatAt(idx: Int, f: Float): F[Unit] = tw.wrap(buf.putFloat(idx, f))
  def putDouble(d: Double): F[Unit] = tw.wrap(buf.putDouble(d))
  def putDoubleAt(idx: Int, d: Double): F[Unit]  = tw.wrap(buf.putDouble(idx, d))
}


object NIOCharBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoCharBufferIO.Module[F] =
    new IsoCharBufferIO.Module[F] {
      def allocate(capacity: Int): F[CharBufferIO[F]] =
        tw.wrap(new NIOCharBufferIO[F](CharBuffer.allocate(capacity), tw))

      def wrapArray(shorts: Array[Char]): F[CharBufferIO[F]] =
        tw.wrap(new NIOCharBufferIO[F](CharBuffer.wrap(shorts), tw))
      def wrapArraySlice(shorts: Array[Char], ofs: Int, len: Int): F[CharBufferIO[F]] =
        tw.wrap(new NIOCharBufferIO[F](CharBuffer.wrap(shorts, ofs, len), tw))

      def wrapCharSequence(csq: CharSequence): F[CharBufferReadIO[F]] =
        tw.wrap(new NIOCharBufferReadIO[F](CharBuffer.wrap(csq), tw))
      def wrapCharSequenceSlice(csq: CharSequence, start: Int, end: Int): F[CharBufferReadIO[F]] =
        tw.wrap(new NIOCharBufferReadIO[F](CharBuffer.wrap(csq, start, end), tw))

      type IORO = NIOCharBufferReadIO[F]
      type IORW = NIOCharBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, CharBuffer] =
        new IsoMutableRORW[IORO, IORW, CharBuffer] {
          def toMutable(io: NIOCharBufferReadIO[F]): CharBuffer = io.buf
          def toIORO(sb: CharBuffer): NIOCharBufferReadIO[F] = new NIOCharBufferReadIO(sb, tw)
          def toIORW(sb: CharBuffer): Option[NIOCharBufferIO[F]] =
            if (sb.isReadOnly) None
            else Some(new NIOCharBufferIO(sb, tw))
        }

    }
}

class NIOCharBufferReadIO[F[_]](val buf: CharBuffer, val tw: ThunkWrap[F])
  extends NIOBufferReadIO[F, Char] with CharBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[CharBufferReadIO[F]] = tw.wrap(new NIOCharBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[CharBufferReadIO[F]] = tw.wrap(new NIOCharBufferReadIO[F](buf.slice, tw))
  def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F]] =
    tw.wrap(new NIOCharBufferReadIO[F](buf.subSequence(start, end), tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def charAt(ofs: Int): F[Char] = tw.wrap(buf.charAt(ofs))
  def get: F[Char] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Char] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Char]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Char], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))

  def read(dst: CharBufferIO[F])(implicit fm: FlatMap[F]): F[Int] =
    dst match {
      case nio: NIOCharBufferIO[F] => tw.wrap(buf.read(nio.buf))
      case dst: CharBufferIO[F] =>
        if (dst.hasArray)
          tw.wrap { throw new Exception("not implemented yet") ; 0 } // TODO: figure out read's real behavior and emulate it
        else
          tw.wrap { throw new Exception("not implemented yet") ; 0 } // TODO: figure out read's real behavior and emulate it
    }
}

class NIOCharBufferIO[F[_]](buf: CharBuffer, tw: ThunkWrap[F])
  extends NIOCharBufferReadIO[F](buf, tw) with NIOBufferIO[F, Char] with CharBufferIO[F] {

  override def duplicateRO: F[CharBufferReadIO[F]] = tw.wrap(new NIOCharBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[CharBufferReadIO[F]] = tw.wrap(new NIOCharBufferReadIO(buf.slice.asReadOnlyBuffer(), tw))
  override def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F]] =
    tw.wrap(new NIOCharBufferReadIO[F](buf.subSequence(start, end).asReadOnlyBuffer, tw))

  def duplicateRW: F[CharBufferIO[F]] = tw.wrap(new NIOCharBufferIO(buf.duplicate, tw))
  def sliceRW: F[CharBufferIO[F]] = tw.wrap(new NIOCharBufferIO(buf.slice, tw))
  def subSequenceRW(start: Int, end: Int): F[CharBufferIO[F]] =
    tw.wrap(new NIOCharBufferIO[F](buf.subSequence(start, end), tw))

  def array: Array[Char] = buf.array

  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(f: Char): F[Unit] = tw.wrap(buf.put(f))
  def putArray(fs: Array[Char]): F[Unit] = tw.wrap(buf.put(fs))
  def putArraySlice(ss: Array[Char], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(ss, ofs, len))
  def putAt(idx: Int, s: Char): F[Unit] = tw.wrap(buf.put(idx, s))
  def putString(s: String): F[Unit] = tw.wrap(buf.put(s))
  def putStringSlice(s: String, start: Int, end: Int): F[Unit] = tw.wrap(buf.put(s, start, end))

  def putBuffer(in: CharBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Char](this, tw, fm)
    in match {
      case nio: NIOCharBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Char]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: CharBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}

object NIOShortBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoShortBufferIO.Module[F] =
    new IsoShortBufferIO.Module[F] {
      def allocate(capacity: Int): F[ShortBufferIO[F]] =
        tw.wrap(new NIOShortBufferIO[F](ShortBuffer.allocate(capacity), tw))

      def wrapArray(shorts: Array[Short]): F[ShortBufferIO[F]] =
        tw.wrap(new NIOShortBufferIO[F](ShortBuffer.wrap(shorts), tw))
      def wrapArraySlice(shorts: Array[Short], ofs: Int, len: Int): F[ShortBufferIO[F]] =
        tw.wrap(new NIOShortBufferIO[F](ShortBuffer.wrap(shorts, ofs, len), tw))

      type IORO = NIOShortBufferReadIO[F]
      type IORW = NIOShortBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, ShortBuffer] =
        new IsoMutableRORW[IORO, IORW, ShortBuffer] {
          def toMutable(io: NIOShortBufferReadIO[F]): ShortBuffer = io.buf
          def toIORO(sb: ShortBuffer): NIOShortBufferReadIO[F] = new NIOShortBufferReadIO(sb, tw)
          def toIORW(sb: ShortBuffer): Option[NIOShortBufferIO[F]] =
            if (sb.isReadOnly) None
            else Some(new NIOShortBufferIO(sb, tw))
        }

    }
}

class NIOShortBufferReadIO[F[_]](val buf: ShortBuffer, val tw: ThunkWrap[F])
  extends NIOBufferReadIO[F, Short] with ShortBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[ShortBufferReadIO[F]] = tw.wrap(new NIOShortBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[ShortBufferReadIO[F]] = tw.wrap(new NIOShortBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Short] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Short] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Short]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Short], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIOShortBufferIO[F[_]](buf: ShortBuffer, tw: ThunkWrap[F])
  extends NIOShortBufferReadIO[F](buf, tw) with NIOBufferIO[F, Short] with ShortBufferIO[F] {

  override def duplicateRO: F[ShortBufferReadIO[F]] = tw.wrap(new NIOShortBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[ShortBufferReadIO[F]] = tw.wrap(new NIOShortBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[ShortBufferIO[F]] = tw.wrap(new NIOShortBufferIO(buf.duplicate, tw))
  def sliceRW: F[ShortBufferIO[F]] = tw.wrap(new NIOShortBufferIO(buf.slice, tw))

  def array: Array[Short] = buf.array

  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(f: Short): F[Unit] = tw.wrap(buf.put(f))
  def putArray(fs: Array[Short]): F[Unit] = tw.wrap(buf.put(fs))
  def putArraySlice(ss: Array[Short], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(ss, ofs, len))
  def putAt(idx: Int, s: Short): F[Unit] = tw.wrap(buf.put(idx, s))

  def putBuffer(in: ShortBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Short](this, tw, fm)
    in match {
      case nio: NIOShortBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Short]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: ShortBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object NIOIntBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoIntBufferIO.Module[F] =
    new IsoIntBufferIO.Module[F] {
      def allocate(capacity: Int): F[IntBufferIO[F]] =
        tw.wrap(new NIOIntBufferIO[F](IntBuffer.allocate(capacity), tw))

      def wrapArray(ints: Array[Int]): F[IntBufferIO[F]] =
        tw.wrap(new NIOIntBufferIO[F](IntBuffer.wrap(ints), tw))
      def wrapArraySlice(ints: Array[Int], ofs: Int, len: Int): F[IntBufferIO[F]] =
        tw.wrap(new NIOIntBufferIO[F](IntBuffer.wrap(ints, ofs, len), tw))

      type IORO = NIOIntBufferReadIO[F]
      type IORW = NIOIntBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, IntBuffer] =
        new IsoMutableRORW[IORO, IORW, IntBuffer] {
          def toMutable(io: NIOIntBufferReadIO[F]): IntBuffer = io.buf
          def toIORO(db: IntBuffer): NIOIntBufferReadIO[F] = new NIOIntBufferReadIO(db, tw)
          def toIORW(db: IntBuffer): Option[NIOIntBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new NIOIntBufferIO(db, tw))
        }

    }
}

class NIOIntBufferReadIO[F[_]](val buf: IntBuffer, val tw: ThunkWrap[F])
      extends NIOBufferReadIO[F, Int] with IntBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[IntBufferReadIO[F]] = tw.wrap(new NIOIntBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[IntBufferReadIO[F]] = tw.wrap(new NIOIntBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Int] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Int] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Int]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Int], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIOIntBufferIO[F[_]](buf: IntBuffer, tw: ThunkWrap[F])
      extends NIOIntBufferReadIO[F](buf, tw) with NIOBufferIO[F, Int] with IntBufferIO[F] {

  def array: Array[Int] = buf.array

  override def duplicateRO: F[IntBufferReadIO[F]] = tw.wrap(new NIOIntBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[IntBufferReadIO[F]] = tw.wrap(new NIOIntBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[IntBufferIO[F]] = tw.wrap(new NIOIntBufferIO(buf.duplicate, tw))
  def sliceRW: F[IntBufferIO[F]] = tw.wrap(new NIOIntBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(f: Int): F[Unit] = tw.wrap(buf.put(f))
  def putArray(fs: Array[Int]): F[Unit] = tw.wrap(buf.put(fs))
  def putArraySlice(fs: Array[Int], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(fs, ofs, len))
  def putAt(idx: Int, f: Int): F[Unit] = tw.wrap(buf.put(idx, f))

  def putBuffer(in: IntBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Int](this, tw, fm)
    in match {
      case nio: NIOIntBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Int]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: IntBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object NIOLongBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoLongBufferIO.Module[F] =
    new IsoLongBufferIO.Module[F] {
      def allocate(capacity: Int): F[LongBufferIO[F]] =
        tw.wrap(new NIOLongBufferIO[F](LongBuffer.allocate(capacity), tw))

      def wrapArray(floats: Array[Long]): F[LongBufferIO[F]] =
        tw.wrap(new NIOLongBufferIO[F](LongBuffer.wrap(floats), tw))
      def wrapArraySlice(floats: Array[Long], ofs: Int, len: Int): F[LongBufferIO[F]] =
        tw.wrap(new NIOLongBufferIO[F](LongBuffer.wrap(floats, ofs, len), tw))

      type IORO = NIOLongBufferReadIO[F]
      type IORW = NIOLongBufferIO[F]

      def isoMutable: IsoMutableRORW[IORO, IORW, LongBuffer] =
        new IsoMutableRORW[IORO, IORW, LongBuffer] {
          def toMutable(io: NIOLongBufferReadIO[F]): LongBuffer = io.buf
          def toIORO(db: LongBuffer): NIOLongBufferReadIO[F] = new NIOLongBufferReadIO(db, tw)
          def toIORW(db: LongBuffer): Option[NIOLongBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new NIOLongBufferIO(db, tw))
        }

    }
}

class NIOLongBufferReadIO[F[_]](val buf: LongBuffer, val tw: ThunkWrap[F])
  extends NIOBufferReadIO[F, Long] with LongBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[LongBufferReadIO[F]] = tw.wrap(new NIOLongBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[LongBufferReadIO[F]] = tw.wrap(new NIOLongBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Long] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Long] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Long]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Long], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIOLongBufferIO[F[_]](buf: LongBuffer, tw: ThunkWrap[F])
  extends NIOLongBufferReadIO[F](buf, tw) with NIOBufferIO[F, Long] with LongBufferIO[F] {

  def array: Array[Long] = buf.array

  override def duplicateRO: F[LongBufferReadIO[F]] = tw.wrap(new NIOLongBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[LongBufferReadIO[F]] = tw.wrap(new NIOLongBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[LongBufferIO[F]] = tw.wrap(new NIOLongBufferIO(buf.duplicate, tw))
  def sliceRW: F[LongBufferIO[F]] = tw.wrap(new NIOLongBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(f: Long): F[Unit] = tw.wrap(buf.put(f))
  def putArray(fs: Array[Long]): F[Unit] = tw.wrap(buf.put(fs))
  def putArraySlice(fs: Array[Long], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(fs, ofs, len))
  def putAt(idx: Int, f: Long): F[Unit] = tw.wrap(buf.put(idx, f))

  def putBuffer(in: LongBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Long](this, tw, fm)
    in match {
      case nio: NIOLongBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Long]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: LongBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object NIOFloatBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoFloatBufferIO.Module[F] =
    new IsoFloatBufferIO.Module[F] {
      def allocate(capacity: Int): F[FloatBufferIO[F]] =
        tw.wrap(new NIOFloatBufferIO[F](FloatBuffer.allocate(capacity), tw))

      def wrapArray(floats: Array[Float]): F[FloatBufferIO[F]] =
        tw.wrap(new NIOFloatBufferIO[F](FloatBuffer.wrap(floats), tw))
      def wrapArraySlice(floats: Array[Float], ofs: Int, len: Int): F[FloatBufferIO[F]] =
        tw.wrap(new NIOFloatBufferIO[F](FloatBuffer.wrap(floats, ofs, len), tw))

      type IORO = NIOFloatBufferReadIO[F]
      type IORW = NIOFloatBufferIO[F]

      def isoMutable: IsoMutableRORW[IORO, IORW, FloatBuffer] =
        new IsoMutableRORW[IORO, IORW, FloatBuffer] {
          def toMutable(io: NIOFloatBufferReadIO[F]): FloatBuffer = io.buf
          def toIORO(db: FloatBuffer): NIOFloatBufferReadIO[F] = new NIOFloatBufferReadIO(db, tw)
          def toIORW(db: FloatBuffer): Option[NIOFloatBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new NIOFloatBufferIO(db, tw))
        }

    }
}

class NIOFloatBufferReadIO[F[_]](val buf: FloatBuffer, val tw: ThunkWrap[F])
      extends NIOBufferReadIO[F, Float] with FloatBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[FloatBufferReadIO[F]] = tw.wrap(new NIOFloatBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[FloatBufferReadIO[F]] = tw.wrap(new NIOFloatBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Float] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Float] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Float]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Float], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIOFloatBufferIO[F[_]](buf: FloatBuffer, tw: ThunkWrap[F])
      extends NIOFloatBufferReadIO[F](buf, tw) with NIOBufferIO[F, Float] with FloatBufferIO[F] {

  def array: Array[Float] = buf.array

  override def duplicateRO: F[FloatBufferReadIO[F]] = tw.wrap(new NIOFloatBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[FloatBufferReadIO[F]] = tw.wrap(new NIOFloatBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[FloatBufferIO[F]] = tw.wrap(new NIOFloatBufferIO(buf.duplicate, tw))
  def sliceRW: F[FloatBufferIO[F]] = tw.wrap(new NIOFloatBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw.wrap(buf.compact)

  def put(f: Float): F[Unit] = tw.wrap(buf.put(f))
  def putArray(fs: Array[Float]): F[Unit] = tw.wrap(buf.put(fs))
  def putArraySlice(fs: Array[Float], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.put(fs, ofs, len))
  def putAt(idx: Int, f: Float): F[Unit] = tw.wrap(buf.put(idx, f))

  def putBuffer(in: FloatBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Float](this, tw, fm)
    in match {
      case nio: NIOFloatBufferIO[F] => tw.wrap(buf.put(nio.buf))
      case rwBuf: BufferIO[F, Float]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: FloatBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object NIODoubleBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoDoubleBufferIO.Module[F] =
    new IsoDoubleBufferIO.Module[F] {
      def allocate(capacity: Int): F[DoubleBufferIO[F]] =
        tw.wrap(new NIODoubleBufferIO[F](DoubleBuffer.allocate(capacity), tw))

      def wrapArray(doubles: Array[Double]): F[DoubleBufferIO[F]] =
        tw.wrap(new NIODoubleBufferIO[F](DoubleBuffer.wrap(doubles), tw))
      def wrapArraySlice(doubles: Array[Double], ofs: Int, len: Int): F[DoubleBufferIO[F]] =
        tw.wrap(new NIODoubleBufferIO[F](DoubleBuffer.wrap(doubles, ofs, len), tw))

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
    extends NIOBufferReadIO[F, Double] with DoubleBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw.wrap(buf.order)

  def get: F[Double] = tw.wrap(buf.get)
  def getAt(idx: Int): F[Double] = tw.wrap(buf.get(idx))
  def getInto(dst: Array[Double]): F[Unit] = tw.wrap(buf.get(dst))
  def getIntoSlice(dst: Array[Double], ofs: Int, len: Int): F[Unit] = tw.wrap(buf.get(dst, ofs, len))
}

class NIODoubleBufferIO[F[_]](buf: DoubleBuffer, tw: ThunkWrap[F])
    extends NIODoubleBufferReadIO[F](buf, tw) with NIOBufferIO[F, Double] with DoubleBufferIO[F] {

  def array: Array[Double] = buf.array

  override def duplicateRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[DoubleBufferReadIO[F]] = tw.wrap(new NIODoubleBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[DoubleBufferIO[F]] = tw.wrap(new NIODoubleBufferIO(buf.duplicate, tw))
  def sliceRW: F[DoubleBufferIO[F]] = tw.wrap(new NIODoubleBufferIO(buf.slice, tw))

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