package com.harana.modules.core.cache

import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.blemale.scaffeine.{Scaffeine, Cache => SCache}
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import zio.{Task, ZIO, ZLayer}

import scala.concurrent.duration._

object LiveCache {
  val layer = ZLayer {
    for {
      config      <- ZIO.service[Config]
      logger      <- ZIO.service[Logger]
      micrometer  <- ZIO.service[Micrometer]
    } yield LiveCache(config, logger, micrometer)
  }
}

case class LiveCache(config: Config, logger: Logger, micrometer: Micrometer) extends Cache {

  def newCache[K, V](expirationSeconds: Long, removalListener: Option[(K, V, RemovalCause) => Unit] = None): Task[SCache[K, V]] = {
      ZIO.attempt {
        val cache = Scaffeine().recordStats().expireAfterWrite(expirationSeconds.seconds)
        if (removalListener.nonEmpty) cache.removalListener(removalListener.get)
        cache.build[K, V]()
      }
    }
}