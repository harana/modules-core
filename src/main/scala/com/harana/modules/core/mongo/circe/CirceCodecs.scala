package com.harana.modules.core.mongo.circe

import io.circe._
import org.mongodb.scala.bson.ObjectId

import scala.util.Try

object CirceCodecs {

  implicit val decodeMoney: Decoder[ObjectId] =
    Decoder.decodeString.emap { str => Try(new ObjectId(str)).toEither.left.map(_.getMessage) }

  implicit val encodeObjectId: Encoder[ObjectId] =
    Encoder.encodeString.contramap[ObjectId](_.toHexString)

}
