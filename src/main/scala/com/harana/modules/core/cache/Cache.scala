package com.harana.modules.core.cache

import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.blemale.scaffeine.{Cache => SCache}
import zio.Task
import zio.macros.accessible

@accessible
trait Cache {
    def newCache[K, V](expirationSeconds: Long, removalListener: Option[(K, V, RemovalCause) => Unit] = None): Task[SCache[K, V]]
}