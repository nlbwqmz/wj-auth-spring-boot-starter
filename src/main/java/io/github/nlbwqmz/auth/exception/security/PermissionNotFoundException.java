package io.github.nlbwqmz.auth.exception.security;

/**
 * 权限未找到异常
 *
 * @author 魏杰
 * @since 0.0.1
 */
public class PermissionNotFoundException extends AuthSecurityException {

  public PermissionNotFoundException(String message) {
    super(message);
  }
}
