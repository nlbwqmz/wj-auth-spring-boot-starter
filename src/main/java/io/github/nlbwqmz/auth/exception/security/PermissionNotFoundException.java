package io.github.nlbwqmz.auth.exception.security;

/**
 * 权限未找到异常
 *
 * @author 魏杰
 * @since 0.0.1
 */
public class PermissionNotFoundException extends AuthSecurityException {

  private static final long serialVersionUID = -4669516593143531404L;

  public PermissionNotFoundException(String message) {
    super(message);
  }
}
