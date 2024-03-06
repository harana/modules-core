package com.harana.modules.core.micrometer

import io.micrometer.core.instrument.Timer.Sample
import io.micrometer.core.instrument._
import io.micrometer.core.instrument.search.{RequiredSearch, Search}
import zio.macros.accessible
import zio.{Task, UIO}

@accessible
trait Micrometer {
    def config: Task[MeterRegistry#Config]

    def clear: Task[Unit]

    def counter(name: String, tags: Map[String, String] = Map()): Task[Counter]

    def find(name: String): Task[Search]

    def gauge[T <: Number](name: String, tags: Map[String, String] = Map(), number: T): Task[T]

    def get(name: String): Task[RequiredSearch]

    def getMeters: Task[List[Meter]]

    def registry: UIO[MeterRegistry]

    def startTimer: Task[Sample]

    def stopTimer(sample: Sample, name: String, tags: Map[String, String] = Map()): Task[Long]

    def summary(name: String, tags: Map[String, String] = Map()): Task[DistributionSummary]

    def timer(name: String, tags: Map[String, String] = Map()): Task[Timer]
}