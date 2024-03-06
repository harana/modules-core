package com.harana.modules.core.app

import com.harana.modules.core.Layers
import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.mongo.Mongo
import com.harana.sdk.shared.models.common.User
import zio._

abstract class App extends zio.ZIOAppDefault {

  def configStr(s: String) = Config.string(s).provideLayer(Layers.config)
  def env(s: String) = Config.env(s).provideLayer(Layers.config)
  def secret(s: String) = Config.secret(s).provideLayer(Layers.config)

  def optConfig(s: String) = Config.optString(s).provideLayer(Layers.config)
  def optEnv(s: String) = Config.optEnv(s).provideLayer(Layers.config)
  def optSecret(s: String) = Config.optSecret(s).provideLayer(Layers.config)

  def logInfo(s: String) = Logger.info(s).provideLayer(Layers.logger)
  def logError(s: String) = Logger.error(s).provideLayer(Layers.logger)

  def startup: ZIO[Any, Any, Unit]
  def shutdown: ZIO[Any, Any, Unit]

  def additionalMongoIndexes: UIO[Unit] = ZIO.unit

  def defaultMongoIndexes =
    for {
      _ <- Mongo.createIndex[User]("Users", Map("emailAddress" -> 1)).provideLayer(Layers.mongo)
      _ <- Mongo.createIndex[User]("Users", Map("id" -> 1), true).provideLayer(Layers.mongo)
      _ <- Mongo.createIndex[User]("Users", Map("id" -> 1, "subscriptionUpdated" -> 1), true).provideLayer(Layers.mongo)
      _ <- Mongo.createIndex[User]("Users", Map("subscriptionCustomerId" -> 1)).provideLayer(Layers.mongo)
    } yield ()


  def run =
    for {
      cluster       <- env("harana_cluster")
      domain        <- env("harana_domain")
      environment   <- env("harana_environment")
      _             <- logInfo(s"Harana Cluster: $cluster")
      _             <- logInfo(s"Harana Domain: $domain")
      _             <- logInfo(s"Harana Environment: $environment")

//      _             <- defaultMongoIndexes.ignore
//      _             <- additionalMongoIndexes.ignore

      _             <- ZIO.succeed(java.lang.Runtime.getRuntime.addShutdownHook(new Thread {
                          override def run() = {
                            println("------------------------------------------------ A")
                            val _ = Unsafe.unsafe { implicit unsafe =>
                                shutdown
                            }
                            println("------------------------------------------------ B")
                          }
                        }))

      exitCode      <- startup.onError(e => logError(e.prettyPrint)).exitCode
    } yield exitCode
}

object App {
  def runEffect[A](effect: ZIO[Any, Any, A]): A =
    Unsafe.unsafe { implicit unsafe => {
      Runtime.default.unsafe.run(effect).getOrThrowFiberFailure()
    }}
}