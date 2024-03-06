package com.harana.modules.core.config

import com.typesafe.config.{Config, ConfigException}
import net.ceedubs.ficus.readers.ValueReader

import java.nio.file.{InvalidPathException, Path}

trait PathReaders {
  implicit val javaPathReader: ValueReader[Path] = new ValueReader[Path] {
    def read(config: Config, path: String): Path = {
      val s = config.getString(path)
      try Path.of(s)
      catch {
        case e: InvalidPathException =>
          throw new ConfigException.WrongType(config.origin(), path, "java.nio.file.Path", "String", e)
      }
    }
  }

}

object PathReaders extends PathReaders
