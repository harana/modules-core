package com.harana.modules.core.mongo.bson.convert

import io.circe.JsonNumber

sealed trait JsonError                       extends Product with Serializable
case class JsonNumberError(json: JsonNumber) extends JsonError
