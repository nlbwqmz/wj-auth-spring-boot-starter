package io.github.nlbwqmz.auth.exception.security;

import io.github.nlbwqmz.auth.exception.AuthException;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class AuthSecurityException extends AuthException {

  private static final long serialVersionUID = -8668883699439972698L;

  public AuthSecurityException() {
  }

  public AuthSecurityException(String msg) {
    super(msg);
  }
}
