package flightready.java.nio.charset

import scala.collection.JavaConverters.mapAsScalaMap

import java.nio.charset.{Charset, StandardCharsets}

import flightready.integration.effect.{PureWrap, ThunkWrap}


object JVMRequiredCharsetIO {
  def apply[F[_]](implicit pw: PureWrap[F]): RequiredCharsetIO.Module[F] =
    new RequiredCharsetIO.Module[F] {
      def in[G[_]](implicit pwG: PureWrap[G]): RequiredCharsetIO.Module[G] = JVMRequiredCharsetIO[G]

      type CS = java.nio.charset.Charset
      def requiredCharSetIO: RequiredCharsetIO[F, Charset] = new JVMRequiredCharsetIO[F]
    }
}

class JVMRequiredCharsetIO[F[_]](implicit pw: PureWrap[F]) extends RequiredCharsetIO[F, java.nio.charset.Charset] {
  def defaultCharSet: F[Charset] = pw(Charset.defaultCharset)

  def usASCII: F[Charset] = pw(StandardCharsets.US_ASCII)
  def iso8859_1: F[Charset] = pw(StandardCharsets.ISO_8859_1)
  def utf8: F[Charset] = pw(StandardCharsets.UTF_8)
  def utf16: F[Charset] = pw(StandardCharsets.UTF_16)
  def utf16BE: F[Charset] = pw(StandardCharsets.UTF_16BE)
  def utf16LE: F[Charset] = pw(StandardCharsets.UTF_16LE)
}

object JVMCharsetIO {
  def apply[F[_]](implicit tw: ThunkWrap[F]): CharsetIO.Module[F] = JVMCharsetIO[F]
}

class JVMCharsetIO[F[_]](implicit tw: ThunkWrap[F]) extends CharsetIO[F, java.nio.charset.Charset] {
  def isSupported(name: String): F[Boolean] = tw(Charset.isSupported(name))
  def forName(name: String): F[Charset] = tw(Charset.forName(name))
  def availableCharSets: F[Map[String, Charset]] =
    tw(mapAsScalaMap(Charset.availableCharsets).toMap)
}
