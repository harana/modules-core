package com.harana.modules.core.mongo.bson.convert

import io.circe.DecodingFailure
import org.bson.BsonType
import org.mongodb.scala.bson.BsonValue

sealed trait BsonError                                              extends Product with Serializable
case class BsonNumberError(bson: BsonValue)                         extends BsonError
case class UnsupportedBsonType(bsonType: BsonType, bson: BsonValue) extends BsonError
case class DecodingError(decodingFailure: DecodingFailure)          extends BsonError
case class Unknown(throwable: Throwable)                            extends BsonError
