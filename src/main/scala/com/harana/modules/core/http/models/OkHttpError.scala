package com.harana.modules.core.http.models

sealed trait OkHttpError
object OkHttpError {
  case class BadRequest(response: String) extends OkHttpError
  case class Unauthorised(response: String) extends OkHttpError
  case class PaymentRequired(response: String) extends OkHttpError
  case class Forbidden(response: String) extends OkHttpError
  case class NotFound(response: String) extends OkHttpError
  case class MethodNotAllowed(response: String) extends OkHttpError
  case class NotAcceptable(response: String) extends OkHttpError
  case class ProxyAuthenticationRequired(response: String) extends OkHttpError
  case class RequestTimeout(response: String) extends OkHttpError
  case class Conflict(response: String) extends OkHttpError
  case class TooManyRequests(response: String) extends OkHttpError
  case class InternalServerError(response: String) extends OkHttpError
  case class NotImplemented(response: String) extends OkHttpError
  case class BadGateway(response: String) extends OkHttpError
  case class ServiceUnavailable(response: String) extends OkHttpError
  case class GatewayTimeout(response: String) extends OkHttpError
  case class Other(code: Int, response: String) extends OkHttpError
  case class InvalidJSON(t: Throwable) extends OkHttpError
  case class Unexpected(t: Throwable) extends OkHttpError
}