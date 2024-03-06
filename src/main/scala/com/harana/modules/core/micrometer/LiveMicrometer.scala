package com.harana.modules.core.micrometer

import com.harana.modules.core.micrometer.LiveMicrometer.registryRef
import io.github.mweirauch.micrometer.jvm.extras._
import io.micrometer.core.instrument.Timer.Sample
import io.micrometer.core.instrument._
import io.micrometer.core.instrument.binder.jvm._
import io.micrometer.core.instrument.binder.logging._
import io.micrometer.core.instrument.binder.system._
import io.micrometer.core.instrument.search.{RequiredSearch, Search}
import io.micrometer.prometheus.{PrometheusConfig, PrometheusMeterRegistry}
import zio.{Task, UIO, ULayer, ZIO, ZLayer}

import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._


object LiveMicrometer {
  val registryRef = new AtomicReference[Option[MeterRegistry]](None)

  val layer =
    ZLayer {
      ZIO.succeed(LiveMicrometer())
    }
}

case class LiveMicrometer() extends Micrometer {

  def config: Task[MeterRegistry#Config] =
    for {
      r <- registry
      s <- ZIO.attempt(r.config)
    } yield s


  def clear: Task[Unit] =
    for {
      r <- registry
      s <- ZIO.attempt(r.clear())
    } yield s


  def counter(name: String, tags: Map[String, String] = Map()): Task[Counter] =
    for {
      r <- registry
      s <- ZIO.attempt(r.counter(name, toTags(tags)))
    } yield s


  def find(name: String): Task[Search] =
    for {
      r <- registry
      s <- ZIO.attempt(r.find(name))
    } yield s


  def gauge[T <: Number](name: String, tags: Map[String, String] = Map(), number: T): Task[T] =
    for {
      r <- registry
      s <- ZIO.attempt(r.gauge(name, toTags(tags), number))
    } yield s


  def get(name: String): Task[RequiredSearch] =
    for {
      r <- registry
      s <- ZIO.attempt(r.get(name))
    } yield s


  def getMeters: Task[List[Meter]] =
    for {
      r <- registry
      s <- ZIO.attempt(r.getMeters.asScala.toList)
    } yield s


  def registry: UIO[MeterRegistry] =
    ZIO.succeed {
      if (registryRef.get.nonEmpty)
        registryRef.get.get
      else {
        val registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        new ClassLoaderMetrics().bindTo(registry)
        new FileDescriptorMetrics().bindTo(registry)
        new JvmCompilationMetrics().bindTo(registry)
        new JvmGcMetrics().bindTo(registry)
        new JvmHeapPressureMetrics().bindTo(registry)
        new JvmMemoryMetrics().bindTo(registry)
        new JvmThreadMetrics().bindTo(registry)
        new Log4j2Metrics().bindTo(registry)
        new ProcessorMetrics().bindTo(registry)
        new ProcessMemoryMetrics().bindTo(registry)
        new ProcessThreadMetrics().bindTo(registry)
        new UptimeMetrics().bindTo(registry)

        Metrics.addRegistry(registry)
        registryRef.set(Some(registry))
        registry
      }
    }


  def startTimer: Task[Sample] =
    for {
      r <- registry
      s <- ZIO.attempt(Timer.start(r))
    } yield s


  def stopTimer(sample: Sample, name: String, tags: Map[String, String] = Map()): Task[Long] =
    for {
      r <- registry
      s <- ZIO.attempt(sample.stop(r.timer(name, toTags(tags))))
    } yield s


  def summary(name: String, tags: Map[String, String] = Map()): Task[DistributionSummary] =
    for {
      r <- registry
      s <- ZIO.attempt(r.summary(name, toTags(tags)))
    } yield s


  def timer(name: String, tags: Map[String, String] = Map()): Task[Timer] =
    for {
      r <- registry
      s <- ZIO.attempt(r.timer(name, toTags(tags)))
    } yield s

  @inline
  private def toTags(map: Map[String, String]) =
    map.map { case (k, v) => Tag.of(k, v) }.asJava
}