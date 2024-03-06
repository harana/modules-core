package com.harana.modules.core

import com.harana.modules.core.mongo.bson.convert.ConvertImplicits._
import com.harana.sdk.shared.models.common.Entity.EntityId
import com.harana.sdk.shared.models.common.Id
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.bson.conversions.Bson
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Sorts
import org.mongodb.scala.model.Sorts.{ascending, descending}
import org.mongodb.scala.result.UpdateResult
import org.mongodb.scala.{FindObservable, MongoCollection, MongoDatabase, Observable, Observer}
import zio.{Task, ZIO}

import java.util.concurrent.atomic.AtomicLong
import java.util.{Calendar, Date}
import scala.collection.mutable.ListBuffer
import scala.reflect.runtime.universe._

package object mongo {

  def byIdSelector(id: EntityId) =
    BsonDocument("id" -> id)

  def sortDocument(fields: List[(String, Boolean)]) = {
    val sortList = fields.map(s => if (s._2) ascending(s._1) else descending(s._1))
    Sorts.orderBy(sortList: _*)
  }

  def getCollection(db: MongoDatabase, collectionName: String): Task[MongoCollection[BsonDocument]] =
    ZIO.attempt(db.getCollection[BsonDocument](collectionName))


  def execute[E](fn: => Observable[E]): Task[E] =
    ZIO.async { (cb: Task[E] => Unit) =>
      fn.subscribe(new Observer[E] {
        override def onNext(result: E): Unit = cb(ZIO.succeed(result))
        override def onError(e: Throwable): Unit = cb(ZIO.fail(e))
        override def onComplete(): Unit = {}
      })
    }


  def executeGet[E](fn: => Observable[BsonDocument])(implicit tt: TypeTag[E], d: Decoder[E]): Task[Option[E]] =
    ZIO.async { (cb: Task[Option[E]] => Unit) =>
      fn.subscribe(new Observer[BsonDocument] {
        private val counter = new AtomicLong(0)

        override def onNext(result: BsonDocument): Unit = {
          counter.incrementAndGet()
          result.fromBson[E] match {
            case Left(x) => cb(ZIO.fail(new Exception(s"${x.mkString(", ")} -> ${result.toString}")))
            case Right(x) => cb(ZIO.some(x))
          }
        }

        override def onError(e: Throwable): Unit =
          e match {
            case _: NoSuchElementException => cb(ZIO.none)
            case _ => cb(ZIO.fail(e))
          }

        override def onComplete(): Unit = {
          if (counter.get() == 0) cb(ZIO.none)
        }
      })
    }


  def executeFind[E](fn: => FindObservable[BsonDocument], limit: Option[Int], skip: Option[Int])(implicit tt: TypeTag[E], d: Decoder[E]): Task[List[E]] =
    ZIO.async { (cb: Task[List[E]] => Unit) =>
      fn.limit(limit.getOrElse(0)).skip(skip.getOrElse(0)).subscribe(new Observer[BsonDocument] {
        private val buffer = new ListBuffer[BsonDocument]()

        override def onNext(result: BsonDocument): Unit = buffer.append(result)

        override def onError(e: Throwable): Unit = {
          e match {
            case _: NoSuchElementException => ZIO.succeed(List())
            case _ => cb(ZIO.fail(e))
          }
        }

        override def onComplete(): Unit = {
          val errors = buffer.map(_.fromBson[E]).filter(_.isLeft).flatMap(_.left.get).toList
          if (errors.nonEmpty) {
            cb(ZIO.fail(new Exception(errors.mkString("\n"))))
          } else {
            cb(ZIO.attempt(buffer.map(_.fromBson[E]).filter(_.isRight).map(_.toOption.get).toList))
          }
        }
      })
    }


  def executeUpdate(collection: MongoCollection[_], id: EntityId, bson: Bson): Task[Unit] =
     ZIO.async { (cb: Task[Unit] => Unit) =>
      collection.updateOne(byIdSelector(id), bson).subscribe(new Observer[UpdateResult] {
        def onNext(result: UpdateResult): Unit = cb(
          if (!result.wasAcknowledged()) ZIO.fail(new Exception("Result was not acknowledged"))
          else if (result.getMatchedCount == 0) ZIO.fail(new Exception("Entity not found"))
          else if (result.getModifiedCount == 0) ZIO.fail(new Exception("Entity not modified"))
          else ZIO.unit
        )

        def onError(t: Throwable): Unit = cb(ZIO.fail(t))

        def onComplete(): Unit = cb(ZIO.unit)
      })
    }


  def executeReplace(collection: MongoCollection[BsonDocument], id: EntityId, bson: BsonDocument): Task[Unit] =
    ZIO.async { (cb: Task[Unit] => Unit) =>
      collection.replaceOne(byIdSelector(id), bson).subscribe(new Observer[UpdateResult] {
        def onNext(result: UpdateResult): Unit = cb(
          if (!result.wasAcknowledged()) ZIO.fail(new Exception("Result was not acknowledged"))
          else if (result.getMatchedCount == 0) ZIO.fail(new Exception("Entity not found"))
          else if (result.getModifiedCount == 0) ZIO.fail(new Exception("Entity not modified"))
          else ZIO.unit
        )

        def onError(t: Throwable): Unit = cb(ZIO.fail(t))

        def onComplete(): Unit = cb(ZIO.unit)
      })
    }


  def nowWithSeconds(seconds: Int): Date = {
    val calendar = Calendar.getInstance
    calendar.add(Calendar.SECOND, seconds)
    calendar.getTime
  }

  def convertToBson[E](entity: E)(implicit e: Encoder[E]) =
    ZIO.fromEither(entity.toBson.left.map(err => new Exception(err.mkString))).map(_.asDocument())


  case class Message[E](id: EntityId,
                        ack: Option[String] = None,
                        visible: Option[Date] = None,
                        deleted: Option[Date] = None,
                        tries: Int = 0,
                        payload: E) extends Id

  implicit def encodeMessage[E](implicit encoder: Encoder[E]): Encoder[Message[E]] =
    Encoder.instance[Message[E]] { m =>
      Json.obj(
        ("id", Json.fromString(m.id)),
        ("ack", Json.fromString(m.ack.orNull)),
        ("visible", Json.fromLong(m.visible.map(_.getTime).getOrElse(-1L))),
        ("deleted", Json.fromLong(m.deleted.map(_.getTime).getOrElse(-1L))),
        ("tries", Json.fromInt(m.tries)),
        ("payload", encoder(m.payload))
      )
    }

  implicit def decodeMessage[E](implicit decoder: Decoder[E]): Decoder[Message[E]] =
    (c: HCursor) => for {
      idField       <- c.downField("id").as[EntityId]
      ackField      <- c.downField("ack").as[String]
      visibleField  <- c.downField("visible").as[Long]
      deletedField  <- c.downField("deleted").as[Long]
      triesField    <- c.downField("tries").as[Int]
      payloadField  <- c.downField("payload").as[E]
    } yield {
      Message(
        idField,
        Option(ackField),
        if (visibleField.equals(-1L)) None else Some(new Date(visibleField)),
        if (deletedField.equals(-1L)) None else Some(new Date(deletedField)),
        triesField,
        payloadField
      )
    }
}