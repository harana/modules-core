package com.harana.modules.core.logger

import org.apache.commons.io.FilenameUtils
import org.apache.logging.log4j.{LogManager, Logger => Log4JLogger}
import sourcecode._
import zio._

import java.util.concurrent.ConcurrentHashMap

object LiveLogger {
  val layer: ULayer[LiveLogger] =
    ZLayer {
      ZIO.succeed(LiveLogger())
    }
}

case class LiveLogger() extends Logger {

 private val loggers = new ConcurrentHashMap[String, Log4JLogger]()

  def error(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).error(message))

  def warn(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).warn(message))

  def info(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).info(message))

  def debug(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).debug(message))

  def trace(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).trace(message))

  def error(t: Throwable)(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).error(message, t))

  def warn(t: Throwable)(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).warn(message, t))

  def info(t: Throwable)(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).info(message, t))

  def debug(t: Throwable)(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).debug(message, t))

  def trace(t: Throwable)(message: => String)(implicit fullName: FullName): UIO[Unit] =
    ZIO.succeed(logger(fullName).trace(message, t))

  private def logger(fullName: FullName) =
    if (loggers.contains(fullName.value))
      loggers.get(fullName.value)
    else {
      val name = FilenameUtils.getBaseName(fullName.value)
      val logger = LogManager.getLogger(name)
      loggers.putIfAbsent(name, logger)
      logger
    }
}