package flightready.java.nio

import java.nio._

import flightready.integration.category.FlatMap
import flightready.integration.effect.ThunkWrap
import flightready.java.nio.file.PutBufferFallback
import flightready.IsoMutableRORW


trait BufferAndWrap[F[_]] {
  private[nio] def buf: Buffer
  private[nio] def tw: ThunkWrap[F]
}

trait JVMBufferReadIO[F[_], A] extends BufferReadIO[F, A] { self: BufferAndWrap[F] =>
  def isDirect: F[Boolean] = tw(buf.isDirect)
  def capacity: F[Int] = tw(buf.capacity)

  def limit: F[Int] = tw(buf.limit)
  def mark: F[Unit] = tw { buf.mark; () }
  def position: F[Int] = tw(buf.position)
  def hasRemaining: F[Boolean] = tw(buf.hasRemaining)
  def remaining: F[Int] = tw(buf.remaining)

  def setLimit(limit: Int): F[Unit] = tw { buf.limit(limit); () }
  def setPosition(position: Int): F[Unit] = tw { buf.position(position); () }

  def clear: F[Unit] = tw { buf.clear; () }
  def flip: F[Unit] = tw { buf.flip; () }
  def reset: F[Unit] = tw { buf.reset; () }
  def rewind: F[Unit] = tw { buf.rewind; () }
}

trait JVMBufferIO[F[_], A] extends BufferIO[F, A] { self: BufferAndWrap[F] =>
  def hasArray: F[Boolean] = tw(buf.hasArray)
  def arrayOffset: F[Int] = tw(buf.arrayOffset)
}

object JVMByteBufferModule {
  def apply[F[_]](implicit tw: ThunkWrap[F]): JVMByteBufferModule[F] = new JVMByteBufferModule[F]
}

class JVMByteBufferModule[F[_]](implicit tw: ThunkWrap[F]) extends IsoByteBufferIO.Module[F] {
  def allocate(capacity: Int): F[JVMByteBufferIO[F]] =
    tw(new JVMByteBufferIO[F](ByteBuffer.allocate(capacity), tw))

  def allocateDirect(capacity: Int): F[JVMByteBufferIO[F]] =
    tw(new JVMByteBufferIO[F](ByteBuffer.allocateDirect(capacity), tw))

  def wrapArray(shorts: Array[Byte]): F[JVMByteBufferIO[F]] =
    tw(new JVMByteBufferIO[F](ByteBuffer.wrap(shorts), tw))
  def wrapArraySlice(shorts: Array[Byte], ofs: Int, len: Int): F[JVMByteBufferIO[F]] =
    tw(new JVMByteBufferIO[F](ByteBuffer.wrap(shorts, ofs, len), tw))

  type IORO = JVMByteBufferReadIO[F]
  type IORW = JVMByteBufferIO[F]

  def isoMutableRORW: IsoMutableRORW[IORO, IORW, ByteBuffer] =
    new IsoMutableRORW[IORO, IORW, ByteBuffer] {
      def toMutable(io: JVMByteBufferReadIO[F]): ByteBuffer = io.buf
      def toIORO(sb: ByteBuffer): JVMByteBufferReadIO[F] = new JVMByteBufferReadIO(sb, tw)
      def toIORW(sb: ByteBuffer): Option[JVMByteBufferIO[F]] =
        if (sb.isReadOnly) None
        else Some(new JVMByteBufferIO(sb, tw))
    }
}

object JVMByteBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): JVMByteBufferModule[F] = new JVMByteBufferModule[F]
}

class JVMByteBufferReadIO[F[_]](private[nio] val buf: ByteBuffer, val tw: ThunkWrap[F])
  extends JVMBufferReadIO[F, Byte] with ByteBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[ByteBufferReadIO[F]] = tw(new JVMByteBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[ByteBufferReadIO[F]] = tw(new JVMByteBufferReadIO[F](buf.slice, tw))

  def asCharBufferRO: F[CharBufferReadIO[F]] = tw(new JVMCharBufferReadIO[F](buf.asCharBuffer, tw))
  def asShortBufferRO: F[ShortBufferReadIO[F]] = tw(new JVMShortBufferReadIO[F](buf.asShortBuffer, tw))
  def asIntBufferRO: F[IntBufferReadIO[F]] = tw(new JVMIntBufferReadIO[F](buf.asIntBuffer, tw))
  def asLongBufferRO: F[LongBufferReadIO[F]] = tw(new JVMLongBufferReadIO[F](buf.asLongBuffer, tw))
  def asFloatBufferRO: F[FloatBufferReadIO[F]] = tw(new JVMFloatBufferReadIO[F](buf.asFloatBuffer, tw))
  def asDoubleBufferRO: F[DoubleBufferReadIO[F]] = tw(new JVMDoubleBufferReadIO[F](buf.asDoubleBuffer, tw))

  def order: F[ByteOrder] = tw(buf.order)

  def get: F[Byte] = tw(buf.get)
  def getAt(idx: Int): F[Byte] = tw(buf.get(idx))
  def getInto(dst: Array[Byte]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Byte], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }
}

class JVMByteBufferIO[F[_]](buf: ByteBuffer, tw: ThunkWrap[F])
  extends JVMByteBufferReadIO[F](buf, tw) with JVMBufferIO[F, Byte] with ByteBufferIO[F] {

  override def duplicateRO: F[ByteBufferReadIO[F]] = tw(new JVMByteBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[ByteBufferReadIO[F]] = tw(new JVMByteBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[ByteBufferIO[F]] = tw(new JVMByteBufferIO(buf.duplicate, tw))
  def sliceRW: F[ByteBufferIO[F]] = tw(new JVMByteBufferIO(buf.slice, tw))

  def setByteOrder(bo: ByteOrder): F[Unit] = tw { buf.order(bo); () }

  override def asCharBufferRO: F[CharBufferReadIO[F]] =
    tw(new JVMCharBufferReadIO[F](buf.asReadOnlyBuffer.asCharBuffer, tw))
  override def asShortBufferRO: F[ShortBufferReadIO[F]] =
    tw(new JVMShortBufferReadIO[F](buf.asReadOnlyBuffer.asShortBuffer, tw))
  override def asIntBufferRO: F[IntBufferReadIO[F]] =
    tw(new JVMIntBufferReadIO[F](buf.asReadOnlyBuffer.asIntBuffer, tw))
  override def asLongBufferRO: F[LongBufferReadIO[F]] =
    tw(new JVMLongBufferReadIO[F](buf.asReadOnlyBuffer.asLongBuffer, tw))
  override def asFloatBufferRO: F[FloatBufferReadIO[F]] =
    tw(new JVMFloatBufferReadIO[F](buf.asReadOnlyBuffer.asFloatBuffer, tw))
  override def asDoubleBufferRO: F[DoubleBufferReadIO[F]] =
    tw(new JVMDoubleBufferReadIO[F](buf.asReadOnlyBuffer.asDoubleBuffer, tw))

  def asCharBufferRW: F[CharBufferIO[F]] = tw(new JVMCharBufferIO[F](buf.asCharBuffer, tw))
  def asShortBufferRW: F[ShortBufferIO[F]] = tw(new JVMShortBufferIO[F](buf.asShortBuffer, tw))
  def asIntBufferRW: F[IntBufferIO[F]] = tw(new JVMIntBufferIO[F](buf.asIntBuffer, tw))
  def asLongBufferRW: F[LongBufferIO[F]] = tw(new JVMLongBufferIO[F](buf.asLongBuffer, tw))
  def asFloatBufferRW: F[FloatBufferIO[F]] = tw(new JVMFloatBufferIO[F](buf.asFloatBuffer, tw))
  def asDoubleBufferRW: F[DoubleBufferIO[F]] = tw(new JVMDoubleBufferIO[F](buf.asDoubleBuffer, tw))

  def array: F[Array[Byte]] = tw(buf.array)
  def compact: F[Unit] = tw { buf.compact; () }

  def put(f: Byte): F[Unit] = tw { buf.put(f); () }
  def putArray(fs: Array[Byte]): F[Unit] = tw { buf.put(fs); () }
  def putArraySlice(ss: Array[Byte], ofs: Int, len: Int): F[Unit] = tw { buf.put(ss, ofs, len); () }
  def putAt(idx: Int, s: Byte): F[Unit] = tw { buf.put(idx, s); () }

  def putBuffer(in: ByteBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Byte](this, tw, fm)
    in match {
      case nio: JVMByteBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Byte]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: ByteBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }

  def putChar(c: Char): F[Unit] = tw { buf.putChar(c); () }
  def putCharAt(idx: Int, c: Char): F[Unit] = tw { buf.putChar(idx, c); () }
  def putShort(s: Short): F[Unit] = tw { buf.putShort(s); () }
  def putShortAt(idx: Int, s: Short): F[Unit] = tw { buf.putShort(idx, s); () }
  def putInt(i: Int): F[Unit] = tw { buf.putInt(i); () }
  def putIntAt(idx: Int, i: Int): F[Unit] = tw { buf.putInt(i, idx); () }
  def putLong(l: Long): F[Unit] = tw { buf.putLong(l); () }
  def putLongAt(idx: Int, l: Long): F[Unit] = tw { buf.putLong(idx, l); () }
  def putFloat(f: Float): F[Unit] = tw { buf.putFloat(f); () }
  def putFloatAt(idx: Int, f: Float): F[Unit] = tw { buf.putFloat(idx, f); () }
  def putDouble(d: Double): F[Unit] = tw { buf.putDouble(d); () }
  def putDoubleAt(idx: Int, d: Double): F[Unit]  = tw { buf.putDouble(idx, d); () }
}


object JVMCharBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoCharBufferIO.Module[F] =
    new IsoCharBufferIO.Module[F] {
      def allocate(capacity: Int): F[JVMCharBufferIO[F]] =
        tw(new JVMCharBufferIO[F](CharBuffer.allocate(capacity), tw))

      def wrapArray(shorts: Array[Char]): F[JVMCharBufferIO[F]] =
        tw(new JVMCharBufferIO[F](CharBuffer.wrap(shorts), tw))
      def wrapArraySlice(shorts: Array[Char], ofs: Int, len: Int): F[JVMCharBufferIO[F]] =
        tw(new JVMCharBufferIO[F](CharBuffer.wrap(shorts, ofs, len), tw))

      def wrapCharSequence(csq: CharSequence): F[JVMCharBufferReadIO[F]] =
        tw(new JVMCharBufferReadIO[F](CharBuffer.wrap(csq), tw))
      def wrapCharSequenceSlice(csq: CharSequence, start: Int, end: Int): F[JVMCharBufferReadIO[F]] =
        tw(new JVMCharBufferReadIO[F](CharBuffer.wrap(csq, start, end), tw))

      type IORO = JVMCharBufferReadIO[F]
      type IORW = JVMCharBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, CharBuffer] =
        new IsoMutableRORW[IORO, IORW, CharBuffer] {
          def toMutable(io: JVMCharBufferReadIO[F]): CharBuffer = io.buf
          def toIORO(sb: CharBuffer): JVMCharBufferReadIO[F] = new JVMCharBufferReadIO(sb, tw)
          def toIORW(sb: CharBuffer): Option[JVMCharBufferIO[F]] =
            if (sb.isReadOnly) None
            else Some(new JVMCharBufferIO(sb, tw))
        }

    }
}

class JVMCharBufferReadIO[F[_]](private[nio] val buf: CharBuffer, val tw: ThunkWrap[F])
  extends JVMBufferReadIO[F, Char] with CharBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[CharBufferReadIO[F]] = tw(new JVMCharBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[CharBufferReadIO[F]] = tw(new JVMCharBufferReadIO[F](buf.slice, tw))
  def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F]] =
    tw(new JVMCharBufferReadIO[F](buf.subSequence(start, end), tw))

  def order: F[ByteOrder] = tw(buf.order)

  def charAt(ofs: Int): F[Char] = tw(buf.charAt(ofs))
  def get: F[Char] = tw(buf.get)
  def getAt(idx: Int): F[Char] = tw(buf.get(idx))
  def getInto(dst: Array[Char]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Char], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }

  def read(dst: CharBufferIO[F])(implicit fm: FlatMap[F]): F[Int] =
    dst match {
      case nio: JVMCharBufferIO[F] => tw(buf.read(nio.buf))
      case _: CharBufferIO[F] =>
        tw { throw new Exception("not implemented yet") }
//        if (dst.hasArray)
//          tw { throw new Exception("not implemented yet") }
//        else
//          tw { throw new Exception("not implemented yet") }
    }
}

class JVMCharBufferIO[F[_]](buf: CharBuffer, tw: ThunkWrap[F])
  extends JVMCharBufferReadIO[F](buf, tw) with JVMBufferIO[F, Char] with CharBufferIO[F] {

  override def duplicateRO: F[CharBufferReadIO[F]] = tw(new JVMCharBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[CharBufferReadIO[F]] = tw(new JVMCharBufferReadIO(buf.slice.asReadOnlyBuffer(), tw))
  override def subSequenceRO(start: Int, end: Int): F[CharBufferReadIO[F]] =
    tw(new JVMCharBufferReadIO[F](buf.subSequence(start, end).asReadOnlyBuffer, tw))

  def duplicateRW: F[CharBufferIO[F]] = tw(new JVMCharBufferIO(buf.duplicate, tw))
  def sliceRW: F[CharBufferIO[F]] = tw(new JVMCharBufferIO(buf.slice, tw))
  def subSequenceRW(start: Int, end: Int): F[CharBufferIO[F]] =
    tw(new JVMCharBufferIO[F](buf.subSequence(start, end), tw))

  def array: F[Array[Char]] = tw(buf.array)

  def compact: F[Unit] = tw { buf.compact; () }

  def put(f: Char): F[Unit] = tw { buf.put(f); () }
  def putArray(fs: Array[Char]): F[Unit] = tw { buf.put(fs); () }
  def putArraySlice(ss: Array[Char], ofs: Int, len: Int): F[Unit] = tw { buf.put(ss, ofs, len); () }
  def putAt(idx: Int, s: Char): F[Unit] = tw { buf.put(idx, s); () }
  def putString(s: String): F[Unit] = tw { buf.put(s); () }
  def putStringSlice(s: String, start: Int, end: Int): F[Unit] = tw { buf.put(s, start, end); () }

  def putBuffer(in: CharBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Char](this, tw, fm)
    in match {
      case nio: JVMCharBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Char]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: CharBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}

object JVMShortBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoShortBufferIO.Module[F] =
    new IsoShortBufferIO.Module[F] {
      def allocate(capacity: Int): F[JVMShortBufferIO[F]] =
        tw(new JVMShortBufferIO[F](ShortBuffer.allocate(capacity), tw))

      def wrapArray(shorts: Array[Short]): F[JVMShortBufferIO[F]] =
        tw(new JVMShortBufferIO[F](ShortBuffer.wrap(shorts), tw))
      def wrapArraySlice(shorts: Array[Short], ofs: Int, len: Int): F[JVMShortBufferIO[F]] =
        tw(new JVMShortBufferIO[F](ShortBuffer.wrap(shorts, ofs, len), tw))

      type IORO = JVMShortBufferReadIO[F]
      type IORW = JVMShortBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, ShortBuffer] =
        new IsoMutableRORW[IORO, IORW, ShortBuffer] {
          def toMutable(io: JVMShortBufferReadIO[F]): ShortBuffer = io.buf
          def toIORO(sb: ShortBuffer): JVMShortBufferReadIO[F] = new JVMShortBufferReadIO(sb, tw)
          def toIORW(sb: ShortBuffer): Option[JVMShortBufferIO[F]] =
            if (sb.isReadOnly) None
            else Some(new JVMShortBufferIO(sb, tw))
        }

    }
}

class JVMShortBufferReadIO[F[_]](private[nio] val buf: ShortBuffer, val tw: ThunkWrap[F])
  extends JVMBufferReadIO[F, Short] with ShortBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[ShortBufferReadIO[F]] = tw(new JVMShortBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[ShortBufferReadIO[F]] = tw(new JVMShortBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw(buf.order)

  def get: F[Short] = tw(buf.get)
  def getAt(idx: Int): F[Short] = tw(buf.get(idx))
  def getInto(dst: Array[Short]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Short], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }
}

class JVMShortBufferIO[F[_]](buf: ShortBuffer, tw: ThunkWrap[F])
  extends JVMShortBufferReadIO[F](buf, tw) with JVMBufferIO[F, Short] with ShortBufferIO[F] {

  override def duplicateRO: F[ShortBufferReadIO[F]] = tw(new JVMShortBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[ShortBufferReadIO[F]] = tw(new JVMShortBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[ShortBufferIO[F]] = tw(new JVMShortBufferIO(buf.duplicate, tw))
  def sliceRW: F[ShortBufferIO[F]] = tw(new JVMShortBufferIO(buf.slice, tw))

  def array: F[Array[Short]] = tw(buf.array)

  def compact: F[Unit] = tw { buf.compact; () }

  def put(f: Short): F[Unit] = tw { buf.put(f); () }
  def putArray(fs: Array[Short]): F[Unit] = tw { buf.put(fs); () }
  def putArraySlice(ss: Array[Short], ofs: Int, len: Int): F[Unit] = tw { buf.put(ss, ofs, len); () }
  def putAt(idx: Int, s: Short): F[Unit] = tw { buf.put(idx, s); () }

  def putBuffer(in: ShortBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Short](this, tw, fm)
    in match {
      case nio: JVMShortBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Short]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: ShortBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object JVMIntBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoIntBufferIO.Module[F] =
    new IsoIntBufferIO.Module[F] {
      def allocate(capacity: Int): F[JVMIntBufferIO[F]] =
        tw(new JVMIntBufferIO[F](IntBuffer.allocate(capacity), tw))

      def wrapArray(ints: Array[Int]): F[JVMIntBufferIO[F]] =
        tw(new JVMIntBufferIO[F](IntBuffer.wrap(ints), tw))
      def wrapArraySlice(ints: Array[Int], ofs: Int, len: Int): F[JVMIntBufferIO[F]] =
        tw(new JVMIntBufferIO[F](IntBuffer.wrap(ints, ofs, len), tw))

      type IORO = JVMIntBufferReadIO[F]
      type IORW = JVMIntBufferIO[F]

      def isoMutableRORW: IsoMutableRORW[IORO, IORW, IntBuffer] =
        new IsoMutableRORW[IORO, IORW, IntBuffer] {
          def toMutable(io: JVMIntBufferReadIO[F]): IntBuffer = io.buf
          def toIORO(db: IntBuffer): JVMIntBufferReadIO[F] = new JVMIntBufferReadIO(db, tw)
          def toIORW(db: IntBuffer): Option[JVMIntBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new JVMIntBufferIO(db, tw))
        }

    }
}

class JVMIntBufferReadIO[F[_]](private[nio] val buf: IntBuffer, val tw: ThunkWrap[F])
      extends JVMBufferReadIO[F, Int] with IntBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[IntBufferReadIO[F]] = tw(new JVMIntBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[IntBufferReadIO[F]] = tw(new JVMIntBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw(buf.order)

  def get: F[Int] = tw(buf.get)
  def getAt(idx: Int): F[Int] = tw(buf.get(idx))
  def getInto(dst: Array[Int]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Int], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }
}

class JVMIntBufferIO[F[_]](buf: IntBuffer, tw: ThunkWrap[F])
      extends JVMIntBufferReadIO[F](buf, tw) with JVMBufferIO[F, Int] with IntBufferIO[F] {

  def array: F[Array[Int]] = tw(buf.array)

  override def duplicateRO: F[IntBufferReadIO[F]] = tw(new JVMIntBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[IntBufferReadIO[F]] = tw(new JVMIntBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[IntBufferIO[F]] = tw(new JVMIntBufferIO(buf.duplicate, tw))
  def sliceRW: F[IntBufferIO[F]] = tw(new JVMIntBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw { buf.compact; () }

  def put(f: Int): F[Unit] = tw { buf.put(f); () }
  def putArray(fs: Array[Int]): F[Unit] = tw { buf.put(fs); () }
  def putArraySlice(fs: Array[Int], ofs: Int, len: Int): F[Unit] = tw { buf.put(fs, ofs, len); () }
  def putAt(idx: Int, f: Int): F[Unit] = tw { buf.put(idx, f); () }

  def putBuffer(in: IntBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Int](this, tw, fm)
    in match {
      case nio: JVMIntBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Int]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: IntBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object JVMLongBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoLongBufferIO.Module[F] =
    new IsoLongBufferIO.Module[F] {
      def allocate(capacity: Int): F[JVMLongBufferIO[F]] =
        tw(new JVMLongBufferIO[F](LongBuffer.allocate(capacity), tw))

      def wrapArray(floats: Array[Long]): F[JVMLongBufferIO[F]] =
        tw(new JVMLongBufferIO[F](LongBuffer.wrap(floats), tw))
      def wrapArraySlice(floats: Array[Long], ofs: Int, len: Int): F[JVMLongBufferIO[F]] =
        tw(new JVMLongBufferIO[F](LongBuffer.wrap(floats, ofs, len), tw))

      type IORO = JVMLongBufferReadIO[F]
      type IORW = JVMLongBufferIO[F]

      def isoMutable: IsoMutableRORW[IORO, IORW, LongBuffer] =
        new IsoMutableRORW[IORO, IORW, LongBuffer] {
          def toMutable(io: JVMLongBufferReadIO[F]): LongBuffer = io.buf
          def toIORO(db: LongBuffer): JVMLongBufferReadIO[F] = new JVMLongBufferReadIO(db, tw)
          def toIORW(db: LongBuffer): Option[JVMLongBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new JVMLongBufferIO(db, tw))
        }

    }
}

class JVMLongBufferReadIO[F[_]](private[nio] val buf: LongBuffer, val tw: ThunkWrap[F])
  extends JVMBufferReadIO[F, Long] with LongBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[LongBufferReadIO[F]] = tw(new JVMLongBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[LongBufferReadIO[F]] = tw(new JVMLongBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw(buf.order)

  def get: F[Long] = tw(buf.get)
  def getAt(idx: Int): F[Long] = tw(buf.get(idx))
  def getInto(dst: Array[Long]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Long], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }
}

class JVMLongBufferIO[F[_]](buf: LongBuffer, tw: ThunkWrap[F])
  extends JVMLongBufferReadIO[F](buf, tw) with JVMBufferIO[F, Long] with LongBufferIO[F] {

  def array: F[Array[Long]] = tw(buf.array)

  override def duplicateRO: F[LongBufferReadIO[F]] = tw(new JVMLongBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[LongBufferReadIO[F]] = tw(new JVMLongBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[LongBufferIO[F]] = tw(new JVMLongBufferIO(buf.duplicate, tw))
  def sliceRW: F[LongBufferIO[F]] = tw(new JVMLongBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw { buf.compact; () }

  def put(f: Long): F[Unit] = tw { buf.put(f); () }
  def putArray(fs: Array[Long]): F[Unit] = tw { buf.put(fs); () }
  def putArraySlice(fs: Array[Long], ofs: Int, len: Int): F[Unit] = tw { buf.put(fs, ofs, len); () }
  def putAt(idx: Int, f: Long): F[Unit] = tw { buf.put(idx, f); () }

  def putBuffer(in: LongBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Long](this, tw, fm)
    in match {
      case nio: JVMLongBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Long]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: LongBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object JVMFloatBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoFloatBufferIO.Module[F] =
    new IsoFloatBufferIO.Module[F] {
      def allocate(capacity: Int): F[JVMFloatBufferIO[F]] =
        tw(new JVMFloatBufferIO[F](FloatBuffer.allocate(capacity), tw))

      def wrapArray(floats: Array[Float]): F[JVMFloatBufferIO[F]] =
        tw(new JVMFloatBufferIO[F](FloatBuffer.wrap(floats), tw))
      def wrapArraySlice(floats: Array[Float], ofs: Int, len: Int): F[JVMFloatBufferIO[F]] =
        tw(new JVMFloatBufferIO[F](FloatBuffer.wrap(floats, ofs, len), tw))

      type IORO = JVMFloatBufferReadIO[F]
      type IORW = JVMFloatBufferIO[F]

      def isoMutable: IsoMutableRORW[IORO, IORW, FloatBuffer] =
        new IsoMutableRORW[IORO, IORW, FloatBuffer] {
          def toMutable(io: JVMFloatBufferReadIO[F]): FloatBuffer = io.buf
          def toIORO(db: FloatBuffer): JVMFloatBufferReadIO[F] = new JVMFloatBufferReadIO(db, tw)
          def toIORW(db: FloatBuffer): Option[JVMFloatBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new JVMFloatBufferIO(db, tw))
        }

    }
}

class JVMFloatBufferReadIO[F[_]](private[nio] val buf: FloatBuffer, val tw: ThunkWrap[F])
      extends JVMBufferReadIO[F, Float] with FloatBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[FloatBufferReadIO[F]] = tw(new JVMFloatBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[FloatBufferReadIO[F]] = tw(new JVMFloatBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw(buf.order)

  def get: F[Float] = tw(buf.get)
  def getAt(idx: Int): F[Float] = tw(buf.get(idx))
  def getInto(dst: Array[Float]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Float], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }
}

class JVMFloatBufferIO[F[_]](buf: FloatBuffer, tw: ThunkWrap[F])
      extends JVMFloatBufferReadIO[F](buf, tw) with JVMBufferIO[F, Float] with FloatBufferIO[F] {

  def array: F[Array[Float]] = tw(buf.array)

  override def duplicateRO: F[FloatBufferReadIO[F]] = tw(new JVMFloatBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[FloatBufferReadIO[F]] = tw(new JVMFloatBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[FloatBufferIO[F]] = tw(new JVMFloatBufferIO(buf.duplicate, tw))
  def sliceRW: F[FloatBufferIO[F]] = tw(new JVMFloatBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw{ buf.compact; () }

  def put(f: Float): F[Unit] = tw { buf.put(f); () }
  def putArray(fs: Array[Float]): F[Unit] = tw { buf.put(fs); () }
  def putArraySlice(fs: Array[Float], ofs: Int, len: Int): F[Unit] = tw { buf.put(fs, ofs, len); () }
  def putAt(idx: Int, f: Float): F[Unit] = tw { buf.put(idx, f); () }

  def putBuffer(in: FloatBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Float](this, tw, fm)
    in match {
      case nio: JVMFloatBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Float]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: FloatBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}


object JVMDoubleBufferReadIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): IsoDoubleBufferIO.Module[F] =
    new IsoDoubleBufferIO.Module[F] {
      def allocate(capacity: Int): F[JVMDoubleBufferIO[F]] =
        tw(new JVMDoubleBufferIO[F](DoubleBuffer.allocate(capacity), tw))

      def wrapArray(doubles: Array[Double]): F[JVMDoubleBufferIO[F]] =
        tw(new JVMDoubleBufferIO[F](DoubleBuffer.wrap(doubles), tw))
      def wrapArraySlice(doubles: Array[Double], ofs: Int, len: Int): F[JVMDoubleBufferIO[F]] =
        tw(new JVMDoubleBufferIO[F](DoubleBuffer.wrap(doubles, ofs, len), tw))

      type IORO = JVMDoubleBufferReadIO[F]
      type IORW = JVMDoubleBufferIO[F]

      def isoMutable: IsoMutableRORW[IORO, IORW, DoubleBuffer] =
        new IsoMutableRORW[IORO, IORW, DoubleBuffer] {
          def toMutable(io: JVMDoubleBufferReadIO[F]): DoubleBuffer = io.buf
          def toIORO(db: DoubleBuffer): JVMDoubleBufferReadIO[F] = new JVMDoubleBufferReadIO(db, tw)
          def toIORW(db: DoubleBuffer): Option[JVMDoubleBufferIO[F]] =
            if (db.isReadOnly) None
            else Some(new JVMDoubleBufferIO(db, tw))
        }

    }
}

class JVMDoubleBufferReadIO[F[_]](private[nio] val buf: DoubleBuffer, val tw: ThunkWrap[F])
    extends JVMBufferReadIO[F, Double] with DoubleBufferReadIO[F] with BufferAndWrap[F] {

  def duplicateRO: F[DoubleBufferReadIO[F]] = tw(new JVMDoubleBufferReadIO(buf.duplicate, tw))
  def sliceRO: F[DoubleBufferReadIO[F]] = tw(new JVMDoubleBufferReadIO[F](buf.slice, tw))

  def order: F[ByteOrder] = tw(buf.order)

  def get: F[Double] = tw(buf.get)
  def getAt(idx: Int): F[Double] = tw(buf.get(idx))
  def getInto(dst: Array[Double]): F[Unit] = tw { buf.get(dst); () }
  def getIntoSlice(dst: Array[Double], ofs: Int, len: Int): F[Unit] = tw { buf.get(dst, ofs, len); () }
}

class JVMDoubleBufferIO[F[_]](buf: DoubleBuffer, tw: ThunkWrap[F])
    extends JVMDoubleBufferReadIO[F](buf, tw) with JVMBufferIO[F, Double] with DoubleBufferIO[F] {

  def array: F[Array[Double]] = tw(buf.array)

  override def duplicateRO: F[DoubleBufferReadIO[F]] = tw(new JVMDoubleBufferReadIO(buf.asReadOnlyBuffer, tw))
  override def sliceRO: F[DoubleBufferReadIO[F]] = tw(new JVMDoubleBufferReadIO(buf.asReadOnlyBuffer.slice, tw))

  def duplicateRW: F[DoubleBufferIO[F]] = tw(new JVMDoubleBufferIO(buf.duplicate, tw))
  def sliceRW: F[DoubleBufferIO[F]] = tw(new JVMDoubleBufferIO(buf.slice, tw))

  def compact: F[Unit] = tw { buf.compact; () }

  def put(d: Double): F[Unit] = tw { buf.put(d); () }
  def putArray(ds: Array[Double]): F[Unit] = tw { buf.put(ds); () }
  def putArraySlice(ds: Array[Double], ofs: Int, len: Int): F[Unit] = tw { buf.put(ds, ofs, len); () }
  def putAt(idx: Int, d: Double): F[Unit] = tw { buf.put(idx, d); () }

  def putBuffer(in: DoubleBufferReadIO[F])(implicit fm: FlatMap[F]): F[Unit] = {
    val fb = new PutBufferFallback[F, Double](this, tw, fm)
    in match {
      case nio: JVMDoubleBufferIO[F] => tw { buf.put(nio.buf); () }
      case rwBuf: BufferIO[F, Double]@unchecked => fb.putBufferViaBackingArray(rwBuf)
      case in: DoubleBufferReadIO[F] => fb.putBufferViaTempCopy(in)
    }
  }
}