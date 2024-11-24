package io.github.nlbwqmz.auth.core.security;

import io.github.nlbwqmz.auth.common.AuthInfo;
import io.github.nlbwqmz.auth.core.security.configuration.AuthHandlerEntity;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public interface SecurityRealm {

  /**
   * 用户授权
   *
   * @return 权限集合
   */
  Set<String> doAuthorization();

  /**
   * 添加 免登录 Patterns
   *
   * @return 免登录 Patterns 集合
   */
  default Set<AuthInfo> addAnonPatterns() {
    return null;
  }

  /**
   * 添加 权限验证 Patterns
   *
   * @return 权限验证 Patterns 集合
   */
  default Set<AuthInfo> addAuthPatterns() {
    return null;
  }

  /**
   * 添加自定义拦截器
   *
   * @return 自定义拦截器集合
   */
  default Set<AuthHandlerEntity> addCustomHandler() {
    return null;
  }

  void handleError(HttpServletRequest request, HttpServletResponse response, Throwable throwable);
}
