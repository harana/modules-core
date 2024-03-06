package com.harana.modules.core.mongo.bson.codec

import org.bson.codecs._
import org.bson.{BsonReader, BsonWriter}

class Tuple2Codec extends Codec[(_, _)] {
  override def encode(writer: BsonWriter, value: (_, _), encoderContext: EncoderContext): Unit = {
    writer.writeStartArray()
    write(writer, value._1)
    write(writer, value._2)
    writer.writeEndArray()
  }

  override def getEncoderClass: Class[(_, _)] = classOf[(_, _)]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): (_, _) = {
    reader.readStartArray()
    val tuple = (read(reader), read(reader))
    reader.readEndArray()
    tuple
  }
}