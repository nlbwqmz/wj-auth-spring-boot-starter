package io.github.nlbwqmz.auth.exception.security;

/**
 * 凭证过期异常
 *
 * @author 魏杰
 * @since 0.0.1
 */
public class CertificateExpiredException extends AuthSecurityException {

  private static final long serialVersionUID = -3105048966689995488L;

  public CertificateExpiredException(String message) {
    super(message);
  }
}
