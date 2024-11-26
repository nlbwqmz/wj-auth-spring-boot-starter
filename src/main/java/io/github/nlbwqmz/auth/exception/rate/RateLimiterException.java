package io.github.nlbwqmz.auth.exception.rate;

import io.github.nlbwqmz.auth.exception.AuthException;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class RateLimiterException extends AuthException {

  public RateLimiterException() {
  }

  public RateLimiterException(String msg) {
    super(msg);
  }
}
