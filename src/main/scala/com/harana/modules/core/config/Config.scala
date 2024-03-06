package com.harana.modules.core.config

import com.typesafe.config.{Config => TypesafeConfig}
import zio.UIO
import zio.macros.accessible

import java.nio.file.Path
import scala.concurrent.duration.{Duration, FiniteDuration}

@accessible
trait Config {
    def boolean(key: String): UIO[Boolean]
    def double(key: String): UIO[Double]
    def duration(key: String): UIO[Duration]
    def finiteDuration(key: String): UIO[FiniteDuration]
    def int(key: String): UIO[Int]
    def long(key: String): UIO[Long]
    def password(key: String): UIO[String]
    def path(key: String): UIO[Path]
    def string(key: String): UIO[String]

    def boolean(key: String, default: Boolean): UIO[Boolean]
    def double(key: String, default: Double): UIO[Double]
    def duration(key: String, default: Duration): UIO[Duration]
    def finiteDuration(key: String, default: FiniteDuration): UIO[FiniteDuration]
    def int(key: String, default: Int): UIO[Int]
    def long(key: String, default: Long): UIO[Long]
    def path(key: String, default: Path): UIO[Path]
    def string(key: String, default: String): UIO[String]

    def listBoolean(key: String, default: List[Boolean] = List()): UIO[List[Boolean]]
    def listDouble(key: String, default: List[Double] = List()): UIO[List[Double]]
    def listDuration(key: String, default: List[Duration] = List()): UIO[List[Duration]]
    def listFiniteDuration(key: String, default: List[FiniteDuration] = List()): UIO[List[FiniteDuration]]
    def listInt(key: String, default: List[Int] = List()): UIO[List[Int]]
    def listLong(key: String, default: List[Long] = List()): UIO[List[Long]]
    def listPath(key: String, default: List[Path] = List()): UIO[List[Path]]
    def listString(key: String, default: List[String] = List()): UIO[List[String]]

    def optBoolean(key: String): UIO[Option[Boolean]]
    def optDouble(key: String): UIO[Option[Double]]
    def optDuration(key: String): UIO[Option[Duration]]
    def optFiniteDuration(key: String): UIO[Option[FiniteDuration]]
    def optInt(key: String): UIO[Option[Int]]
    def optLong(key: String): UIO[Option[Long]]
    def optPassword(key: String): UIO[Option[String]]
    def optPath(key: String): UIO[Option[Path]]
    def optString(key: String): UIO[Option[String]]

    def optSecret(key: String): UIO[Option[String]]
    def secret(key: String): UIO[String]

    def optEnv(key: String): UIO[Option[String]]
    def env(key: String): UIO[String]

    def underlyingConfig: UIO[TypesafeConfig]
}