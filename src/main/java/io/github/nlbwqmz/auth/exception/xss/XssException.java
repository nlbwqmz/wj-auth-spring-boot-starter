package io.github.nlbwqmz.auth.exception.xss;

import io.github.nlbwqmz.auth.exception.AuthException;

/**
 * @author 魏杰
 * @since 2020/10/17
 */
public class XssException extends AuthException {

  private static final long serialVersionUID = -1636012677889012370L;

  public XssException() {
  }

  public XssException(String msg) {
    super(msg);
  }
}
