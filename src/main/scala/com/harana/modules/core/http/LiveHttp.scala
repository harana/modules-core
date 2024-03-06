package com.harana.modules.core.http

import com.harana.modules.core.config.Config
import com.harana.modules.core.logger.Logger
import com.harana.modules.core.micrometer.Micrometer
import com.harana.modules.core.http.logging.OkHttp3LoggingInterceptor
import com.harana.modules.core.http.logging.core.{Level, LogWriter, LoggerConfig}
import com.harana.modules.core.http.models.OkHttpError
import io.circe.Json
import io.circe.parser._
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener
import okhttp3._
import okio.{BufferedSink, Okio}
import org.apache.logging.log4j.LogManager
import zio.{IO, UIO, ULayer, ZIO, ZLayer}

import java.io.{IOException, InputStream}
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._

object LiveHttp {
  val layer =
    ZLayer {
      for {
        config      <- ZIO.service[Config]
        logger      <- ZIO.service[Logger]
        micrometer  <- ZIO.service[Micrometer]
      } yield LiveHttp(config, logger, micrometer)
    }
}

case class LiveHttp(config: Config, logger: Logger, micrometer: Micrometer) extends Http {

  private val defaultUserAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0"
  private val client = newClient

  def get(url: String,
          params: Map[String, List[String]],
          headers: Map[String, String],
          credentials: Option[(String, String)] = None): IO[OkHttpError, Response] =
    execute(requestBuilder(url, params, headers, credentials, json = false).map(_.get().build()))


  def getAsJson(url: String,
                params: Map[String, List[String]],
                headers: Map[String, String],
                credentials: Option[(String, String)] = None): IO[OkHttpError, Json] =
    parseJson(getAsString(url, params, headers, credentials))


  def getAsStream(url: String,
                  params: Map[String, List[String]],
                  headers: Map[String, String],
                  credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream] =
    get(url, params, headers, credentials).map(_.body.byteStream())


  def getAsString(url: String,
                  params: Map[String, List[String]],
                  headers: Map[String, String],
                  credentials: Option[(String, String)] = None): IO[OkHttpError, String] =
    get(url, params, headers, credentials).map(_.body.string)


  def post(url: String,
           body: Option[String],
           mimeType: Option[String],
           params: Map[String, List[String]],
           headers: Map[String, String],
           credentials: Option[(String, String)] = None): IO[OkHttpError, Response] =
    execute(requestBuilder(url, params, headers, credentials, json = false).map(_.post(requestBody(body, mimeType)).build()))


  def postAsJson(url: String,
                 body: Option[String],
                 mimeType: Option[String],
                 params: Map[String, List[String]],
                 headers: Map[String, String],
                 credentials: Option[(String, String)] = None): IO[OkHttpError, Json] =
    parseJson(postAsString(url, body, mimeType, params, headers, credentials))


  def postAsStream(url: String,
                   body: Option[String],
                   mimeType: Option[String],
                   params: Map[String, List[String]],
                   headers: Map[String, String],
                   credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream] =
    post(url, body, mimeType, params, headers, credentials).map(_.body.byteStream())


  def postAsString(url: String,
                   body: Option[String],
                   mimeType: Option[String],
                   params: Map[String, List[String]],
                   headers: Map[String, String],
                   credentials: Option[(String, String)] = None): IO[OkHttpError, String] =
    post(url, body, mimeType, params, headers, credentials).map(_.body.string)


  def postForm(url: String,
               formBody: Map[String, String],
               params: Map[String, List[String]],
               headers: Map[String, String],
               credentials: Option[(String, String)] = None): IO[OkHttpError, Response] = {
    val formBodyBuilder = new FormBody.Builder()
    formBody.foreach { case (k, v) => formBodyBuilder.add(k, v) }
    execute(requestBuilder(url, params, headers, credentials, json = false).map(_.post(formBodyBuilder.build()).build()))
  }


  def postFormAsJson(url: String,
                     body: Map[String, String],
                     params: Map[String, List[String]],
                     headers: Map[String, String],
                     credentials: Option[(String, String)] = None): IO[OkHttpError, Json] =
    parseJson(postFormAsString(url, body, params, headers, credentials))


  def postFormAsStream(url: String,
                       formBody: Map[String, String],
                       params: Map[String, List[String]],
                       headers: Map[String, String],
                       credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream] =
    postForm(url, formBody, params, headers, credentials).map(_.body.byteStream())


  def postFormAsString(url: String,
                       formBody: Map[String, String],
                       params: Map[String, List[String]],
                       headers: Map[String, String],
                       credentials: Option[(String, String)] = None): IO[OkHttpError, String] =
    postForm(url, formBody, params, headers, credentials).map(_.body.string)


  def binaryPost(url: String,
                 resourcePath: String,
                 params: Map[String, List[String]],
                 headers: Map[String, String],
                 credentials: Option[(String, String)] = None): IO[OkHttpError, Response] = {
    val inputStream = ClassLoader.getSystemClassLoader.getResourceAsStream(resourcePath)

    val requestBody: RequestBody = new RequestBody {
      override def writeTo(sink: BufferedSink): Unit = {
        val source = Okio.source(inputStream)
        sink.writeAll(source)
        source.close()
      }

      override def contentLength(): Long = inputStream.available().toLong

      override def contentType(): MediaType = MediaType.parse("application/binary")
    }

    execute(requestBuilder(url, params, headers, credentials, json = false).map(_.post(requestBody).build()))
  }


  def binaryPostAsJson(url: String,
                       resourcePath: String,
                       params: Map[String, List[String]],
                       headers: Map[String, String],
                       credentials: Option[(String, String)] = None): IO[OkHttpError, Json] =
    parseJson(binaryPostAsString(url, resourcePath, params, headers, credentials))


  def binaryPostAsStream(url: String,
                         resourcePath: String,
                         params: Map[String, List[String]],
                         headers: Map[String, String],
                         credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream] =
    binaryPost(url, resourcePath, params, headers, credentials).map(_.body.byteStream())


  def binaryPostAsString(url: String,
                         resourcePath: String,
                         params: Map[String, List[String]],
                         headers: Map[String, String],
                         credentials: Option[(String, String)] = None): IO[OkHttpError, String] =
    binaryPost(url, resourcePath, params, headers, credentials).map(_.body.string)


  def put(url: String,
          body: Option[String],
          mimeType: Option[String],
          params: Map[String, List[String]],
          headers: Map[String, String],
          credentials: Option[(String, String)] = None): IO[OkHttpError, Response] =
    execute(requestBuilder(url, params, headers, credentials, json = false).map(_.put(requestBody(body, mimeType)).build()))


  def putAsJson(url: String,
                body: Option[String],
                mimeType: Option[String],
                params: Map[String, List[String]],
                headers: Map[String, String],
                credentials: Option[(String, String)] = None): IO[OkHttpError, Json] =
    parseJson(putAsString(url, body, mimeType, params, headers, credentials))


  def putAsStream(url: String,
                  body: Option[String],
                  mimeType: Option[String],
                  params: Map[String, List[String]],
                  headers: Map[String, String],
                  credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream] =
    put(url, body, mimeType, params, headers, credentials).map(_.body.byteStream())


  def putAsString(url: String,
                  body: Option[String],
                  mimeType: Option[String],
                  params: Map[String, List[String]],
                  headers: Map[String, String],
                  credentials: Option[(String, String)] = None): IO[OkHttpError, String] =
    put(url, body, mimeType, params, headers, credentials).map(_.body.string)


  def delete(url: String,
             params: Map[String, List[String]],
             headers: Map[String, String],
             credentials: Option[(String, String)] = None): IO[OkHttpError, Response] =
    execute(requestBuilder(url, params, headers, credentials, json = false).map(_.delete().build()))


  def deleteAsJson(url: String,
                   params: Map[String, List[String]],
                   headers: Map[String, String],
                   credentials: Option[(String, String)] = None): IO[OkHttpError, Json] =
    parseJson(deleteAsString(url, params, headers, credentials))


  def deleteAsStream(url: String,
                     params: Map[String, List[String]],
                     headers: Map[String, String],
                     credentials: Option[(String, String)] = None): IO[OkHttpError, InputStream] =
    delete(url, params, headers, credentials).map(_.body.byteStream())


  def deleteAsString(url: String,
                     params: Map[String, List[String]],
                     headers: Map[String, String],
                     credentials: Option[(String, String)] = None): IO[OkHttpError, String] =
    delete(url, params, headers, credentials).map(_.body.string)


  private def parseJson(fn: => IO[OkHttpError, String]) =
    fn.map(parse(_).toOption.get)


  private def newClient: UIO[OkHttpClient] =
    for {
      registry <- micrometer.registry
      proxyEnabled <- config.boolean("okhttp.proxy.enabled", default = false)
      proxyType <- config.optString("okhttp.proxy.type")
      proxyHost <- config.optString("okhttp.proxy.host")
      proxyPort <- config.optInt("okhttp.proxy.port")
      proxyUsername <- config.optString("okhttp.proxy.username")
      proxyPassword <- config.optString("okhttp.proxy.password")
      proxyDomain <- config.optString("okhttp.proxy.domain")
    } yield {
      new OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(new OkHttp3LoggingInterceptor(LoggerConfig.builder()
          .logger(new LogWriter() {
            val logger = LogManager.getLogger()
            override def log(msg: String): Unit = logger.debug(msg)
          })
          .level(Level.BASIC)
          .loggable(true)
          .maxLineLength(180)
          .withThreadInfo(true)
          .build()
        ))
        .eventListener(OkHttpMetricsEventListener.builder(registry, "okhttp.requests").build())
        .build()
    }

  //  TODO: Add proxy support back in
  //          .proxy(if (!proxyEnabled) Proxy.NO_PROXY else new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)))
  //          .proxyAuthenticator(proxyType match {
  //
  //            case "basic" => (_: Route, response: Response) =>
  //              val credential = Credentials.basic(proxyUsername, proxyPassword)
  //              response.request.newBuilder.header("Proxy-Authorization", credential).build
  //
  //            case "ntlm" => (_: Route, response: Response) =>
  //              val engine = new NTLMEngine()
  //              val ntlmMsg1 = engine.generateType1Msg(null, null)
  //              val WWWAuthenticate = response.headers.values("WWW-Authenticate")
  //              if (WWWAuthenticate.contains("NTLM"))
  //                response.request.newBuilder.header("Authorization", s"NTLM $ntlmMsg1").build
  //              else {
  //                val ntlmMsg3 = engine.generateType3Msg(proxyUsername, proxyPassword, proxyDomain, "android-device", WWWAuthenticate.get(0).substring(5))
  //                response.request.newBuilder.header("Authorization", s"NTLM $ntlmMsg3").build
  //              }
  //          })


  private def requestBuilder(url: String,
                             params: Map[String, List[String]],
                             headers: Map[String, String],
                             credentials: Option[(String, String)],
                             json: Boolean) =
    config.string("okhttp.useragent", defaultUserAgent).map { agent =>
      val urlBuilder = HttpUrl.parse(url).newBuilder()
      params.foreach { case (k, v) => v.foreach(urlBuilder.addQueryParameter(k, _)) }

      val requestBuilder = new Request.Builder()
        .url(urlBuilder.build())
        .headers(Headers.of(headers.asJava))
        .header("User-Agent", agent)
      if (credentials.nonEmpty) requestBuilder.addHeader("Authorization", Credentials.basic(credentials.get._1, credentials.get._2))
      if (json) requestBuilder.addHeader("accept", "application/json")
      requestBuilder
    }


  private def requestBody(body: Option[String], mimeType: Option[String]) =
    if (body.nonEmpty)
      RequestBody.create(body.get, MediaType.parse(mimeType.get + "; charset=utf-8"))
    else
      RequestBody.create("", null)


  private def execute(request: UIO[Request]): IO[OkHttpError, Response] =
    for {
      res <- request
      c <- client
      rsp <- ZIO.async { (cb: IO[OkHttpError, Response] => Unit) =>
        c.newCall(res).enqueue(new Callback() {
          def onFailure(call: Call, e: IOException): Unit =
            cb(ZIO.fail(OkHttpError.Unexpected(e)))

          def onResponse(call: Call, response: Response): Unit =
            cb(
              response.code match {
                case x if x < 300 => ZIO.succeed(response)
                case 400 => ZIO.fail(OkHttpError.BadRequest(response.message()))
                case 401 => ZIO.fail(OkHttpError.Unauthorised(response.message()))
                case 402 => ZIO.fail(OkHttpError.PaymentRequired(response.message()))
                case 403 => ZIO.fail(OkHttpError.Forbidden(response.message()))
                case 404 => ZIO.fail(OkHttpError.NotFound(response.message()))
                case 405 => ZIO.fail(OkHttpError.MethodNotAllowed(response.message()))
                case 406 => ZIO.fail(OkHttpError.NotAcceptable(response.message()))
                case 407 => ZIO.fail(OkHttpError.ProxyAuthenticationRequired(response.message()))
                case 408 => ZIO.fail(OkHttpError.RequestTimeout(response.message()))
                case 409 => ZIO.fail(OkHttpError.Conflict(response.message()))
                case 429 => ZIO.fail(OkHttpError.TooManyRequests(response.message()))
                case 500 => ZIO.fail(OkHttpError.InternalServerError(response.message()))
                case 501 => ZIO.fail(OkHttpError.NotImplemented(response.message()))
                case 502 => ZIO.fail(OkHttpError.BadGateway(response.message()))
                case 503 => ZIO.fail(OkHttpError.ServiceUnavailable(response.message()))
                case 504 => ZIO.fail(OkHttpError.GatewayTimeout(response.message()))
                case x => ZIO.fail(OkHttpError.Other(x, response.message()))
              }
            )
        })
      }
    } yield rsp
}
