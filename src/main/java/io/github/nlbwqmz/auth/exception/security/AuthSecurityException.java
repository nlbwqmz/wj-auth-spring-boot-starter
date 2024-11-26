package io.github.nlbwqmz.auth.exception.security;

import io.github.nlbwqmz.auth.exception.AuthException;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class AuthSecurityException extends AuthException {

  public AuthSecurityException() {
  }

  public AuthSecurityException(String msg) {
    super(msg);
  }
}
