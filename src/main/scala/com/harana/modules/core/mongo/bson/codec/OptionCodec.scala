package com.harana.modules.core.mongo.bson.codec

import org.bson.codecs._
import org.bson.{BsonReader, BsonType, BsonWriter}

class OptionCodec extends Codec[Option[_]] {
  override def encode(writer: BsonWriter, value: Option[_], encoderContext: EncoderContext) = value match {
    case Some(v: Any)       => write(writer, v)
    case None               => writer.writeNull()
  }

  override def getEncoderClass: Class[Option[_]] = classOf[Option[_]]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Option[_] =
    reader.getCurrentBsonType match {
      case BsonType.NULL    => None
      case _                => Some(read(reader))
    }
}