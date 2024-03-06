package com.harana.modules.core

import com.harana.modules.core.cache.{Cache, LiveCache}
import com.harana.modules.core.config.{Config, LiveConfig}
import com.harana.modules.core.logger.{Logger, LiveLogger}
import com.harana.modules.core.micrometer.{LiveMicrometer, Micrometer}
import com.harana.modules.core.mongo.{Mongo, LiveMongo}
import com.harana.modules.core.http.{Http, LiveHttp}
import zio.ZLayer

object Layers {
  val logger = LiveLogger.layer
  val config = ZLayer.make[Config](LiveLogger.layer, LiveConfig.layer)
  val standard = ZLayer.make[Config with Logger with Micrometer](config, LiveLogger.layer, LiveMicrometer.layer)

  val cache = ZLayer.make[Cache](standard, LiveCache.layer)
  val http = ZLayer.make[Http](standard, LiveHttp.layer)
  val mongo = ZLayer.make[Mongo](standard, LiveMongo.layer)
}