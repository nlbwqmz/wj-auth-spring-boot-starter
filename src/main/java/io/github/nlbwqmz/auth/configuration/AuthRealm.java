package io.github.nlbwqmz.auth.configuration;

import io.github.nlbwqmz.auth.common.SecurityInfo;
import io.github.nlbwqmz.auth.core.security.configuration.SecurityHandler;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public interface AuthRealm {

  /**
   * 用户授权
   *
   * @return 权限集合
   */
  default Set<String> userPermission(String subject) {
    return null;
  }

  /**
   * 添加 匿名 Patterns
   *
   * @return 免登录 Patterns 集合
   */
  default Set<SecurityInfo> anonymous() {
    return null;
  }

  /**
   * 添加 授权 Patterns
   *
   * @return 权限验证 Patterns 集合
   */
  default Set<SecurityInfo> authorize() {
    return null;
  }

  /**
   * 添加自定义拦截器
   *
   * @return 自定义拦截器集合
   */
  default Set<SecurityHandler> customSecurityHandler() {
    return null;
  }

  void handleError(HttpServletRequest request, HttpServletResponse response, Throwable throwable);
}
