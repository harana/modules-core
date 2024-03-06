package com.harana.modules.core.config

import com.harana.modules.core.config.LiveConfig.{config, getEnv}
import com.harana.modules.core.config.PathReaders._
import com.harana.modules.core.logger.Logger
import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ValueReader
import zio.{UIO, ZIO, ZLayer}

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.io.Source._

object LiveConfig {
  private lazy val environmentConfig = ConfigFactory.parseResources(s"application_${getEnv("harana_environment").map(_.toLowerCase).getOrElse("")}.conf")
  private lazy val defaultConfig = ConfigFactory.parseResources("application.conf")
  private lazy val config = ConfigFactory.load(environmentConfig).withFallback(defaultConfig).resolve()

  private def getEnv(key: String): Option[String] = {
    sys.env.get(key.toUpperCase) match {
      case Some(x) => Some(x)
      case None => sys.env.get(key.toLowerCase)
    }
  }

  val layer = ZLayer {
    for {
      logger   <- ZIO.service[Logger]
    } yield LiveConfig(logger)
  }
}

case class LiveConfig(logger: Logger) extends Config {

  def boolean(key: String): UIO[Boolean] = get[Boolean](key)
  def double(key: String): UIO[Double] = get[Double](key)
  def duration(key: String): UIO[Duration] = get[Duration](key)
  def finiteDuration(key: String): UIO[FiniteDuration] = get[FiniteDuration](key)
  def int(key: String): UIO[Int] = get[Int](key)
  def long(key: String): UIO[Long] = get[Long](key)
  def password(key: String): UIO[String] = get[String](key)
  def path(key: String): UIO[Path] = get[Path](key)
  def string(key: String): UIO[String] = get[String](key)

  def boolean(key: String, default: Boolean): UIO[Boolean] = getWithDefault[Boolean](key, default)
  def double(key: String, default: Double): UIO[Double] = getWithDefault[Double](key, default)
  def duration(key: String, default: Duration): UIO[Duration] = getWithDefault[Duration](key, default)
  def finiteDuration(key: String, default: FiniteDuration): UIO[FiniteDuration] = getWithDefault[FiniteDuration](key, default)
  def int(key: String, default: Int): UIO[Int] = getWithDefault[Int](key, default)
  def long(key: String, default: Long): UIO[Long] = getWithDefault[Long](key, default)
  def path(key: String, default: Path): UIO[Path] = getWithDefault[Path](key, default)
  def string(key: String, default: String): UIO[String] = getWithDefault[String](key, default)

  def listBoolean(key: String, default: List[Boolean] = List()): UIO[List[Boolean]] = list[Boolean](key, default)
  def listDouble(key: String, default: List[Double] = List()): UIO[List[Double]] = list[Double](key, default)
  def listDuration(key: String, default: List[Duration] = List()): UIO[List[Duration]] = list[Duration](key, default)
  def listFiniteDuration(key: String, default: List[FiniteDuration] = List()): UIO[List[FiniteDuration]] = list[FiniteDuration](key, default)
  def listInt(key: String, default: List[Int] = List()): UIO[List[Int]] = list[Int](key, default)
  def listLong(key: String, default: List[Long] = List()): UIO[List[Long]] = list[Long](key, default)
  def listPath(key: String, default: List[Path] = List()): UIO[List[Path]] = list[Path](key, default)
  def listString(key: String, default: List[String] = List()): UIO[List[String]] = list[String](key, default)

  def optBoolean(key: String): UIO[Option[Boolean]] = opt[Boolean](key)
  def optDouble(key: String): UIO[Option[Double]] = opt[Double](key)
  def optDuration(key: String): UIO[Option[Duration]] = opt[Duration](key)
  def optFiniteDuration(key: String): UIO[Option[FiniteDuration]] = opt[FiniteDuration](key)
  def optInt(key: String): UIO[Option[Int]] = opt[Int](key)
  def optLong(key: String): UIO[Option[Long]] = opt[Long](key)
  def optPassword(key: String): UIO[Option[String]] = opt[String](key)
  def optPath(key: String): UIO[Option[Path]] = opt[Path](key)
  def optString(key: String): UIO[Option[String]] = opt[String](key)

  def optSecret(key: String): UIO[Option[String]] =
    for {
      path          <- string("secrets.path", "/var/run/secrets")
      secret        <- if (Files.exists(Paths.get(s"$path/$key/value"))) {
                        val fileHandle = fromFile(s"$path/$key/value", "utf-8")
                        val secret = fileHandle.getLines.mkString
                        fileHandle.close()
                        ZIO.some(secret)
                      }
                      else
                        ZIO.none
    } yield secret

  def secret(key: String): UIO[String] = {
    for {
      path      <- string("secrets.path", "/var/run/secrets")
      secret    <- optSecret(key).map(_.get).onError(_ => logger.error(s"Missing secret: $path/$key/value"))
    } yield secret
  }

  def optEnv(key: String): UIO[Option[String]] =
    ZIO.succeed(getEnv(key))

  def env(key: String): UIO[String] =
    ZIO.succeed(getEnv(key).get).onError(_ => logger.error(s"Missing environment variable: $key"))

  def underlyingConfig: UIO[TypesafeConfig] = ZIO.succeed(config)

  private def get[A](key: String)(implicit reader: ValueReader[A]) = {
    if (!config.hasPathOrNull(key)) logger.error(s"Missing config: $key")
    ZIO.succeed(config.as[A](key))
  }

  private def getWithDefault[A](key: String, default: A)(implicit reader: ValueReader[A]) = {
    opt[A](key).map(_.getOrElse(default))
  }

  private def list[A](key: String, default: List[A])(implicit reader: ValueReader[Option[List[A]]]) = {
    ZIO.succeed(config.as[Option[List[A]]](key)).map(_.getOrElse(default))
  }

  private def opt[A](key: String)(implicit reader: ValueReader[Option[A]]) = {
    ZIO.attempt(config.as[Option[A]](key)).orElseSucceed(None)
  }
}