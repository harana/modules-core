package com.harana.modules.core.mongo.bson.codec

import com.harana.modules.core.mongo.bson.convert.ConvertImplicits._
import io.circe
import org.bson.codecs._
import org.bson.{BsonReader, BsonWriter}

import scala.reflect.ClassTag

class CirceDocumentCodec[T: circe.Codec: ClassTag] extends Codec[T] {
  private[this] val codec = new BsonDocumentCodec()

  override def decode(reader: BsonReader, decoderContext: DecoderContext): T =
    codec
      .decode(reader, decoderContext)
      .fromBson
      .fold(
        errors => codecError(s"Codec decode errors: $errors"),
        identity
      )

  override def encode(writer: BsonWriter, value: T, encoderContext: EncoderContext): Unit =
    codec.encode(
      writer,
      value.toBson
        .fold(
          errors => codecError(s"Codec encode errors: $errors"),
          _.asDocument()
        ),
      encoderContext
    )

  private def codecError(msg: String): Nothing = throw CodecException(msg)

  override def getEncoderClass: Class[T] = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
}

object CirceDocumentCodec {
  def fromCirceCodec[T: circe.Codec: ClassTag]: CirceDocumentCodec[T] = new CirceDocumentCodec[T]
}
