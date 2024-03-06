package com.harana.modules.core.mongo.models

sealed trait MongoError
object MongoError {
  case object EntityNotFound extends MongoError
  case object ModificationFailure extends MongoError
  case object UnacknowledgedWrite extends MongoError
  case class Unexpected(t: Throwable) extends MongoError
}
