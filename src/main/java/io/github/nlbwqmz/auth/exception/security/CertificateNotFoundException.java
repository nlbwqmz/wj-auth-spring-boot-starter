package io.github.nlbwqmz.auth.exception.security;

/**
 * 凭证未找到异常
 *
 * @author 魏杰
 * @since 0.0.1
 */
public class CertificateNotFoundException extends AuthSecurityException {

  public CertificateNotFoundException() {
    super("certificate not found");
  }
}
