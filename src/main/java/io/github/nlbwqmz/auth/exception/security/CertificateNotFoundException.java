package io.github.nlbwqmz.auth.exception.security;

/**
 * 凭证未找到异常
 *
 * @author 魏杰
 * @since 0.0.1
 */
public class CertificateNotFoundException extends AuthSecurityException {

  private static final long serialVersionUID = -2681373326677425832L;

  public CertificateNotFoundException() {
    super("certificate not found");
  }
}
