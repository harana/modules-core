package com.harana.modules.core.mongo.bson.convert

import cats.instances.list._
import cats.instances.vector._
import cats.syntax.either._
import cats.syntax.traverse._
import io.circe.{Json, JsonNumber, JsonNumberHelper, JsonObject}
import org.bson.BsonType
import org.mongodb.scala.bson._

import scala.jdk.CollectionConverters._

object CirceToBsonConverters {
  def bsonToJson(bson: BsonValue): Either[List[BsonError], Json] = {
    import BsonType._
    import Json._

    def asObject: Either[List[BsonError], Json] =
      bson
        .asDocument()
        .asScala
        .view.mapValues(bsonToJson)
        .map { case (key, value) => value.map(json => (key, json)).toValidated }
        .toList
        .sequence
        .toEither
        .map(entries => obj(entries: _*))

    def asArray: Either[List[BsonError], Json] =
      bson
        .asArray()
        .asScala
        .toList
        .map(bsonToJson)
        .map(_.toValidated)
        .sequence
        .toEither
        .map(jsons => arr(jsons: _*))

    bson.getBsonType match {
      case DOCUMENT              => asObject
      case ARRAY                 => asArray
      case INT32                 => fromInt(bson.asInt32().intValue()).asRight
      case INT64                 => fromLong(bson.asInt64().longValue()).asRight
      case DOUBLE                => Either.fromOption(fromDouble(bson.asDouble().doubleValue()), List(BsonNumberError(bson)))
      case DECIMAL128            => fromBigDecimal(bson.asDecimal128().decimal128Value().bigDecimalValue()).asRight
      case STRING                => fromString(bson.asString().getValue).asRight
      case BOOLEAN               => fromBoolean(bson.asBoolean().getValue).asRight
      case NULL                  => Null.asRight
      case UNDEFINED             => Null.asRight
      case JAVASCRIPT            => fromString(bson.asJavaScript().getCode).asRight
      case JAVASCRIPT_WITH_SCOPE => fromString(bson.asJavaScriptWithScope().getCode).asRight
      case OBJECT_ID             => fromString(bson.asObjectId().getValue.toHexString).asRight
      case SYMBOL                => fromString(bson.asSymbol().getSymbol).asRight
      case TIMESTAMP             => fromLong(bson.asTimestamp().getValue).asRight
      case DATE_TIME             => fromLong(bson.asDateTime().getValue).asRight
      case unsupported           => List(UnsupportedBsonType(unsupported, bson)).asLeft
    }
  }

  def jsonToBson(json: Json): Either[List[JsonError], BsonValue] = json.foldWith(jsonFolder)

  private[this] lazy val jsonFolder: Json.Folder[Either[List[JsonError], BsonValue]] = {
    new Json.Folder[Either[List[JsonError], BsonValue]] {
      override def onNull: Either[List[JsonError], BsonValue]                   = BsonNull().asRight
      override def onBoolean(bool: Boolean): Either[List[JsonError], BsonValue] = BsonBoolean(bool).asRight
      override def onNumber(value: JsonNumber): Either[List[JsonError], BsonValue] =
        JsonNumberHelper.toBsonNumber(value).leftMap(_ :: Nil)
      override def onString(str: String): Either[List[JsonError], BsonValue] =
        if (str == null) BsonString("").asRight else BsonString(str).asRight
      override def onArray(values: Vector[Json]): Either[List[JsonError], BsonValue] =
        values.map(jsonToBson).map(_.toValidated).sequence.toEither.map(BsonArray.fromIterable(_))
      override def onObject(obj: JsonObject): Either[List[JsonError], BsonValue] =
        obj.toMap
          .view.mapValues(jsonToBson)
          .map { case (key, value) => value.map(bson => (key, bson)).toValidated }
          .toList
          .sequence
          .toEither
          .map(BsonDocument(_))
    }
  }
}
