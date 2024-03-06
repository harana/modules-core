package com.harana.modules.core.mongo

import com.harana.sdk.shared.models.common.Entity.EntityId
import io.circe.{Decoder, Encoder}
import org.bson.BsonDocument
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.conversions._
import org.mongodb.scala.model.IndexOptions
import zio.Task
import zio.macros.accessible

import java.nio.ByteBuffer
import scala.reflect.runtime.universe._

@accessible
trait Mongo {

  def ping: Task[Unit]

  def aggregate(collectionName: String, stages: List[Bson]): Task[List[BsonDocument]]

  def dropCollection(collectionName: String): Task[Unit]

  def get[E](collectionName: String, id: EntityId)(implicit tt: TypeTag[E], d: Decoder[E]): Task[Option[E]]

  def insert[E](collectionName: String, entity: E)(implicit tt: TypeTag[E], e: Encoder[E]): Task[Unit]

  def insertMany[E](collectionName: String, entities: List[E])(implicit tt: TypeTag[E], e: Encoder[E]): Task[Unit]

  def upsert[E](collectionName: String, id: EntityId, entity: E)(implicit tt: TypeTag[E], e: Encoder[E]): Task[Unit]

  def update[E](collectionName: String, id: EntityId, entity: E)(implicit tt: TypeTag[E], e: Encoder[E]): Task[Unit]

  def updateFields(collectionName: String, id: EntityId, keyValues: Map[String, Object]): Task[Unit]

  def appendToListField(collectionName: String, id: EntityId, field: String, value: Object): Task[Unit]

  def replace[E](collectionName: String, id: EntityId, entity: E, upsert: Boolean)(implicit tt: TypeTag[E], e: Encoder[E]): Task[Unit]

  def delete[E](collectionName: String, bson: Bson)(implicit tt: TypeTag[E]): Task[Unit]

  def delete[E](collectionName: String, id: EntityId)(implicit tt: TypeTag[E]): Task[Unit]

  def deleteEquals[E](collectionName: String, keyValues: Map[String, Object])(implicit tt: TypeTag[E]): Task[Unit]

  def distinctEquals(collectionName: String, field: String, keyValues: Map[String, Object]): Task[List[String]]

  def findEquals[E](collectionName: String, keyValues: Map[String, Object], sort: List[(String, Boolean)] = List(), limit: Option[Int] = None, skip: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[List[E]]

  def findOne[E](collectionName: String, keyValues: Map[String, Object], sort: List[(String, Boolean)] = List())(implicit tt: TypeTag[E], d: Decoder[E]): Task[Option[E]]

  def findOneAndDelete[E](collectionName: String, findValues: Map[String, Object], sort: List[(String, Boolean)] = List())(implicit tt: TypeTag[E], d: Decoder[E]): Task[Option[E]]

  def find[E](collectionName: String, bson: Bson, sort: List[(String, Boolean)] = List(), limit: Option[Int] = None, skip: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[List[E]]

  def countEquals(collectionName: String, keyValues: Map[String, Object]): Task[Long]

  def count(collectionName: String, bson: Bson): Task[Long]

  def textSearch[E](collectionName: String, text: String, limit: Option[Int] = None, skip: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[List[E]]

  def textSearchFindEquals[E](collectionName: String, text: String, keyValues: Map[String, Object], limit: Option[Int] = None, skip: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[List[E]]

  def all[E](collectionName: String, sort: List[(String, Boolean)] = List(), limit: Option[Int] = None, skip: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[List[E]]

  def createIndex[E](collectionName: String, indexes: Map[String, Int], unique: Boolean = false, opts: Option[IndexOptions] = None)(implicit tt: TypeTag[E]): Task[Unit]

  def createTextIndex[E](collectionName: String, fields: List[String])(implicit tt: TypeTag[E]): Task[Unit]

  def uploadToGridFS(bucketName: String, fileName: String, contents: ByteBuffer): Task[ObjectId]

  def downloadFromGridFS(bucketName: String, objectId: ObjectId): Task[ByteBuffer]

  def deleteFromGridFS(bucketName: String, objectId: ObjectId): Task[Unit]

  def createQueue(queueName: String, viqsibility: Option[Int] = None, delay: Option[Int] = None, maxRetries: Option[Int] = None): Task[Unit]

  def ackQueue[E](queueName: String, id: EntityId, visibility: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[Option[E]]

  def pingQueue[E](queueName: String, id: EntityId, visibility: Option[Int] = None)(implicit tt: TypeTag[E], d: Decoder[E]): Task[Option[E]]

  def addToQueue[E](queueName: String, entities: List[E], delay: Option[Int] = None)(implicit tt: TypeTag[E], e: Encoder[E]): Task[List[E]]

  def getFromQueue[E](queueName: String, visibility: Option[Int] = None)(implicit tt: TypeTag[Message[E]], d: Decoder[Message[E]]): Task[Option[E]]

  def countQueue[E](queueName: String, bson: Bson)(implicit tt: TypeTag[E]): Task[Long]

  def waitingCountForQueue[E](queueName: String)(implicit tt: TypeTag[E]): Task[Long]

  def totalCountForQueue[E](queueName: String)(implicit tt: TypeTag[E]): Task[Long]

  def inFlightCountForQueue[E](queueName: String)(implicit tt: TypeTag[E]): Task[Long]

  def completedCountForQueue[E](queueName: String)(implicit tt: TypeTag[E]): Task[Long]

  def purgeQueue[E](queueName: String)(implicit tt: TypeTag[E]): Task[Unit]

}