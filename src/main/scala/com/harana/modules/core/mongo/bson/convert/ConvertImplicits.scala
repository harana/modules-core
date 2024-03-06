package com.harana.modules.core.mongo.bson.convert

import cats.syntax.either._
import com.harana.modules.core.mongo.bson.convert.CirceToBsonConverters.{bsonToJson, jsonToBson}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.mongodb.scala.bson.BsonValue

object ConvertImplicits {
  implicit class ToBsonOps[T](val value: T) extends AnyVal {
    def toBson(implicit encoder: Encoder[T]): Either[List[JsonError], BsonValue] = jsonToBson(value.asJson)
  }

  implicit class FromBsonOps(val value: BsonValue) extends AnyVal {
    def fromBson[T](implicit decoder: Decoder[T]): Either[List[BsonError], T] = try {
      bsonToJson(value).flatMap(_.as[T].leftMap(df => List(DecodingError(df))))
    } catch {
      case e: Exception => Left(List(Unknown(e)))
    }
  }
}