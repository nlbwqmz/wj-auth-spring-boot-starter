package io.github.nlbwqmz.auth.core.security.configuration;

public interface TokenKeyConfiguration {

  /**
   * HS 算法密钥
   */
  default String key() {
    return null;
  }

  /**
   * RS 算法公钥
   */
  default String publicKey() {
    return null;
  }

  /**
   * RS 算法私钥
   */
  default String privateKey() {
    return null;
  }

}
