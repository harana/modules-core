package com.harana.modules.core.mongo.bson.codec

import org.bson.codecs._
import org.bson.{BsonReader, BsonWriter}

class SomeCodec extends Codec[Some[_]] {
  override def encode(writer: BsonWriter, value: Some[_], encoderContext: EncoderContext) = value match {
    case Some(v: Any)       => write(writer, v)
  }

  override def getEncoderClass: Class[Some[_]] = classOf[Some[_]]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Some[_] =
    reader.getCurrentBsonType match {
      case _                => Some(read(reader))
    }
}