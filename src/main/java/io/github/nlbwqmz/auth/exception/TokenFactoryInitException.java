package io.github.nlbwqmz.auth.exception;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class TokenFactoryInitException extends AuthException {

  private static final long serialVersionUID = -5679819620579358386L;

  public TokenFactoryInitException() {
    super("AuthTokenGenerate init error");
  }

  public TokenFactoryInitException(String msg) {
    super(msg);
  }
}
