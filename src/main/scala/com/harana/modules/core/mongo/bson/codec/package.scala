package com.harana.modules.core.mongo.bson

import org.bson.types.Decimal128
import org.bson.{BsonReader, BsonTimestamp, BsonType, BsonWriter}

import java.time.Instant

package object codec {

  def read(reader: BsonReader) = reader.getCurrentBsonType match {
    case BsonType.STRING        => reader.readString()
    case BsonType.INT32         => Int.box(reader.readInt32())
    case BsonType.INT64         => Long.box(reader.readInt64())
    case BsonType.DOUBLE        => Double.box(reader.readDouble())
    case BsonType.BOOLEAN       => Boolean.box(reader.readBoolean())
    case BsonType.DECIMAL128    => reader.readDecimal128().bigDecimalValue()
    case BsonType.TIMESTAMP     => Instant.ofEpochSecond(reader.readTimestamp().getValue)
  }

  def write(writer: BsonWriter, value: Any) = value match {
    case s: String              => writer.writeString(s)
    case i: Int                 => writer.writeInt32(i)
    case l: Long                => writer.writeInt64(l)
    case d: Double              => writer.writeDouble(d)
    case b: Boolean             => writer.writeBoolean(b)
    case b: BigDecimal          => writer.writeDecimal128(new Decimal128(b.bigDecimal))
    case i: Instant             => writer.writeTimestamp(new BsonTimestamp(i.getEpochSecond))
  }

}
