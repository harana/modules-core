package com.harana.modules.core.http

import com.harana.modules.core.http.models.OkHttpError
import io.circe.Json
import okhttp3.Response
import zio.IO
import zio.macros.accessible

import java.io.InputStream

@accessible
trait Http {

  def get(url: String,
          params: Map[String, List[String]] = Map(),
          headers: Map[String, String] = Map(),
          credentials: Option[(String, String)] = None): IO[OkHttpError, Response]

  def getAsJson(url: String,
                params: Map[String, List[String]] = Map(),
                headers: Map[String, String] = Map(),
                credentials: Option[(String, String)] = None): IO[OkHttpError, Json]

  def getAsStream(url: String,
                  params: Map[String, List[String]] = Map(),
                  headers: Map[String, String] = Map(),
                  credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream]

  def getAsString(url: String,
                  params: Map[String, List[String]] = Map(),
                  headers: Map[String, String] = Map(),
                  credentials: Option[(String, String)] = None): IO[OkHttpError, String]

  def post(url: String,
           body: Option[String] = None,
           mimeType: Option[String] = None,
           params: Map[String, List[String]] = Map(),
           headers: Map[String, String] = Map(),
           credentials: Option[(String, String)] = None): IO[OkHttpError, Response]

  def postAsJson(url: String,
                 body: Option[String] = None,
                 mimeType: Option[String] = None,
                 params: Map[String, List[String]] = Map(),
                 headers: Map[String, String] = Map(),
                 credentials: Option[(String, String)] = None): IO[OkHttpError, Json]

  def postAsStream(url: String,
                   body: Option[String] = None,
                   mimeType: Option[String] = None,
                   params: Map[String, List[String]] = Map(),
                   headers: Map[String, String] = Map(),
                   credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream]

  def postAsString(url: String,
                   body: Option[String] = None,
                   mimeType: Option[String] = None,
                   params: Map[String, List[String]] = Map(),
                   headers: Map[String, String] = Map(),
                   credentials: Option[(String, String)] = None): IO[OkHttpError, String]

  def postForm(url: String,
               formBody: Map[String, String] = Map(),
               params: Map[String, List[String]] = Map(),
               headers: Map[String, String] = Map(),
               credentials: Option[(String, String)] = None): IO[OkHttpError, Response]

  def postFormAsJson(url: String,
                     formBody: Map[String, String] = Map(),
                     params: Map[String, List[String]] = Map(),
                     headers: Map[String, String] = Map(),
                     credentials: Option[(String, String)] = None): IO[OkHttpError, Json]

  def postFormAsStream(url: String,
                       formBody: Map[String, String] = Map(),
                       params: Map[String, List[String]] = Map(),
                       headers: Map[String, String] = Map(),
                       credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream]

  def postFormAsString(url: String,
                       formBody: Map[String, String] = Map(),
                       params: Map[String, List[String]] = Map(),
                       headers: Map[String, String] = Map(),
                       credentials: Option[(String, String)] = None): IO[OkHttpError, String]

  def binaryPost(url: String,
                 resourcePath: String,
                 params: Map[String, List[String]] = Map(),
                 headers: Map[String, String] = Map(),
                 credentials: Option[(String, String)] = None): IO[OkHttpError, Response]

  def binaryPostAsJson(url: String,
                       resourcePath: String,
                       params: Map[String, List[String]] = Map(),
                       headers: Map[String, String] = Map(),
                       credentials: Option[(String, String)] = None): IO[OkHttpError, Json]

  def binaryPostAsStream(url: String,
                         resourcePath: String,
                         params: Map[String, List[String]] = Map(),
                         headers: Map[String, String] = Map(),
                         credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream]

  def binaryPostAsString(url: String,
                         resourcePath: String,
                         params: Map[String, List[String]] = Map(),
                         headers: Map[String, String] = Map(),
                         credentials: Option[(String, String)] = None): IO[OkHttpError, String]

  def put(url: String,
          body: Option[String] = None,
          mimeType: Option[String] = None,
          params: Map[String, List[String]] = Map(),
          headers: Map[String, String] = Map(),
          credentials: Option[(String, String)] = None): IO[OkHttpError, Response]

  def putAsJson(url: String,
                body: Option[String] = None,
                mimeType: Option[String] = None,
                params: Map[String, List[String]] = Map(),
                headers: Map[String, String] = Map(),
                credentials: Option[(String, String)] = None): IO[OkHttpError, Json]

  def putAsStream(url: String,
                  body: Option[String] = None,
                  mimeType: Option[String] = None,
                  params: Map[String, List[String]] = Map(),
                  headers: Map[String, String] = Map(),
                  credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream]

  def putAsString(url: String,
                  body: Option[String] = None,
                  mimeType: Option[String] = None,
                  params: Map[String, List[String]] = Map(),
                  headers: Map[String, String] = Map(),
                  credentials: Option[(String, String)] = None): IO[OkHttpError, String]

  def delete(url: String,
             params: Map[String, List[String]] = Map(),
             headers: Map[String, String] = Map(),
             credentials: Option[(String, String)] = None): IO[OkHttpError, Response]

  def deleteAsJson(url: String,
                   params: Map[String, List[String]] = Map(),
                   headers: Map[String, String] = Map(),
                   credentials: Option[(String, String)] = None): IO[OkHttpError, Json]

  def deleteAsStream(url: String,
                     params: Map[String, List[String]] = Map(),
                     headers: Map[String, String] = Map(),
                     credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream]

  def deleteAsString(url: String,
                     params: Map[String, List[String]] = Map(),
                     headers: Map[String, String] = Map(),
                     credentials: Option[(String, String)] = None): IO[OkHttpError, String]
}