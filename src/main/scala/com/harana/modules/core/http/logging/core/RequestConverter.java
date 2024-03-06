package com.harana.modules.core.http.logging.core;

import com.harana.modules.core.http.logging.core.internal.InterceptedRequest;

/**
 * Base interface for helper classes converting client specific HTTP requests to internal {@link
 * InterceptedRequest}.
 *
 * @param <T> type of Http client specific request
 */
public interface RequestConverter<T> {

  InterceptedRequest from(T request);

}
