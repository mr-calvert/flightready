package flightready.java.nio.charset

import flightready.integration.effect.PureWrap


object RequiredCharsetIO {
  trait Module[F[_]] {
    def in[G[_]](implicit pw: PureWrap[G]): Module[G]

    type CS
    def requiredCharSetIO: RequiredCharsetIO[F, CS]
  }
}

/** Unwrapped accessors for JVM's required CharSets */
trait RequiredCharsetIO[F[_], CS] {
  def defaultCharSet: F[CS]

  def usASCII: F[CS]
  def iso8859_1: F[CS]
  def utf8: F[CS]
  def utf16: F[CS]
  def utf16BE: F[CS]
  def utf16LE: F[CS]
}

object CharsetIO {
  trait Module[F[_]] extends RequiredCharsetIO.Module[F] {
    def charSetIO: CharsetIO[F, CS]
  }
}

trait CharsetIO[F[_], CS] {
  def availableCharSets: F[Map[String, CS]]
  def isSupported(name: String): F[Boolean]
  def forName(name: String): F[CS]
}
