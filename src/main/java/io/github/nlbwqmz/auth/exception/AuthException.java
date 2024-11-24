package io.github.nlbwqmz.auth.exception;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class AuthException extends RuntimeException {

  private static final long serialVersionUID = 4988644127541663322L;

  public AuthException() {
    super();
  }

  public AuthException(String msg) {
    super(msg);
  }

}
