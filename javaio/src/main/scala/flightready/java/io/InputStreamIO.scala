package flightready.java.io


/** Operations upon an opn [[java.io.InputStream]]
  *
  * The close operation has been elided for type safety.
  *
  * NOTE: InputStream's basic construction is dubious from an FP
  * point of view seeing as its `read` methods work by modifying an
  * `Array[Byte]` provided by the caller. The reliance on a
  * procedural style output mutable parameter is a long long way
  * from the spirit of pure FP. FlightReady goes to some lengths to
  * avoid using mutable data types in its interface. Except in this
  * case I'm not doing anything to dress this up.
  *
  * The `readInto` operations in this algebra are, on their own,
  * referentially transparent. Of course most of the obvious things
  * one would write with this algebra are themselves not pure FP.
  * FlightReady's default opinion of such things is negative, however
  * all the obvious ways to dress this up are themselves of dubious
  * utility. Instead we're wrapping this unprincipled interface as it
  * is and assuming you, dear developer, are mature enough to make
  * your own choices.
  *
  * If you're inexperienced and unsure what would constitute a safer
  * choice than this pretty little trap, I suggest using literally
  * any of the other low level IO algebras in FlightReady, or even
  * better the higher level ones. When I get around to writing them
  * that is.
  */
trait InputStreamIO[F[_]] {
  def available: F[Int]

  def reset: F[Unit]

  def mark(readLimit: Int): F[Unit]
  def markSupported: F[Boolean]

  def read: F[Int]
  def readInto(bytesOut: Array[Byte]): F[Int]
  def readIntoSlice(bytesOut: Array[Byte], ofs: Int, len: Int): F[Int]

  def skip(n: Long): F[Long]
}
