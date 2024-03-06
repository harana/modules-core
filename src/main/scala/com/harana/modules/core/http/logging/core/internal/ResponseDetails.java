package com.harana.modules.core.http.logging.core.internal;


/**
 * Helper class for collecting all response details from external library's response and provide it
 * to {@link ResponseHandler} for internal InterceptedResponse instance creation.
 */
@SuppressWarnings("PMD.LinguisticNaming")
public class ResponseDetails {

  public final InterceptedResponseBody responseBody;
  public final InterceptedMediaType mediaType;
  public final InterceptedHeaders headers;
  public final boolean isSuccessful;
  public final Protocol protocol;
  public final String message;
  public final int code;

  ResponseDetails(InterceptedResponseBody responseBody, InterceptedHeaders headers, int code,
      boolean isSuccessful, String message, InterceptedMediaType mediaType, Protocol protocol) {

    this.responseBody = responseBody;
    this.isSuccessful = isSuccessful;
    this.mediaType = mediaType;
    this.protocol = protocol;
    this.message = message;
    this.headers = headers;
    this.code = code;
  }

  public static ResponseDetailsBuilder builder() {
    return new ResponseDetailsBuilder();
  }

  @Override
  public String toString() {
    return "ResponseDetails{"
        + "responseBody=" + responseBody
        + ", headers=" + headers
        + ", code=" + code
        + ", isSuccessful=" + isSuccessful
        + ", message='" + message + '\''
        + ", mediaType=" + mediaType
        + ", protocol=" + protocol
        + '}';
  }

  @SuppressWarnings("JavadocType")
  public static class ResponseDetailsBuilder {

    private InterceptedResponseBody responseBody;
    private InterceptedMediaType mediaType;
    private InterceptedHeaders headers;
    private boolean isSuccessful;
    private Protocol protocol;
    private String message;
    private int code;

    public ResponseDetails.ResponseDetailsBuilder responseBody(
        InterceptedResponseBody responseBody) {
      this.responseBody = responseBody;
      return this;
    }

    public ResponseDetails.ResponseDetailsBuilder mediaType(InterceptedMediaType mediaType) {
      this.mediaType = mediaType;
      return this;
    }

    public ResponseDetails.ResponseDetailsBuilder headers(InterceptedHeaders headers) {
      this.headers = headers;
      return this;
    }

    public ResponseDetails.ResponseDetailsBuilder isSuccessful(boolean isSuccessful) {
      this.isSuccessful = isSuccessful;
      return this;
    }

    public ResponseDetails.ResponseDetailsBuilder protocol(Protocol protocol) {
      this.protocol = protocol;
      return this;
    }

    public ResponseDetails.ResponseDetailsBuilder message(String message) {
      this.message = message;
      return this;
    }

    public ResponseDetails.ResponseDetailsBuilder code(int code) {
      this.code = code;
      return this;
    }

    public ResponseDetails build() {
      return new ResponseDetails(responseBody, headers, code, isSuccessful, message, mediaType,
          protocol);
    }

    @Override
    public String toString() {
      return "ResponseDetails.ResponseDetailsBuilder(responseBody=" + this.responseBody
          + ", headers=" + this.headers
          + ", code=" + this.code
          + ", isSuccessful=" + this.isSuccessful
          + ", message=" + this.message
          + ", mediaType=" + this.mediaType + ")";
    }
  }
}
