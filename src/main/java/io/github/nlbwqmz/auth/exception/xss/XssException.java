package io.github.nlbwqmz.auth.exception.xss;

import io.github.nlbwqmz.auth.exception.AuthException;

/**
 * @author 魏杰
 * @since 2020/10/17
 */
public class XssException extends AuthException {

  public XssException() {
  }

  public XssException(String msg) {
    super(msg);
  }
}
