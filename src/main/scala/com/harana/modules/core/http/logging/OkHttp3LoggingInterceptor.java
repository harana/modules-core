package com.harana.modules.core.http.logging;

import com.harana.modules.core.http.logging.core.AbstractInterceptor;
import com.harana.modules.core.http.logging.core.LoggerConfig;
import com.harana.modules.core.http.logging.core.RequestConverter;
import com.harana.modules.core.http.logging.core.ResponseConverter;
import com.harana.modules.core.http.logging.core.internal.ClientPrintingExecutor;
import com.harana.modules.core.http.logging.core.internal.InterceptedRequest;
import com.harana.modules.core.http.logging.core.internal.InterceptedResponse;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OkHttp3LoggingInterceptor extends AbstractInterceptor implements Interceptor {

  private final RequestConverter<Request> requestConverter;
  private final ResponseConverter<Response> responseConverter;

  public OkHttp3LoggingInterceptor(final LoggerConfig loggerConfig) {
    this.requestConverter = new OkHttp3RequestConverter();
    this.responseConverter = new OkHttp3ResponseConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  @SuppressWarnings("Duplicates")
  public Response intercept(final Chain chain) throws IOException {
    final Request request = chain.request();

    if (skipLogging()) {
      return chain.proceed(request);
    }

    final InterceptedRequest interceptedRequest = requestConverter.from(request);

    ClientPrintingExecutor.printRequest(loggerConfig, interceptedRequest);

    final Response response = chain.proceed(request);
    final InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(),
            response.receivedResponseAtMillis() - response.sentRequestAtMillis());

    ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
