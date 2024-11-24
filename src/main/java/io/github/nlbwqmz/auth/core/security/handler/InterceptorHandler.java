package io.github.nlbwqmz.auth.core.security.handler;

import io.github.nlbwqmz.auth.core.security.configuration.Logical;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public interface InterceptorHandler {

  /**
   * 授权
   */
  boolean authorize(HttpServletRequest request, HttpServletResponse response, String[] auth,
      Logical logical,
      @Nullable Set<String> userAuth);

  /**
   * 认证
   *
   * @return token
   */
  String authenticate(HttpServletRequest request, HttpServletResponse response, String header);


  /**
   * 是否验证token
   */
  default boolean isVerifyToken() {
    return true;
  }

  /**
   * 是否刷新token
   */
  default boolean isRefreshToken() {
    return true;
  }

  /**
   * 是否授权
   */
  default boolean isAuthorize() {
    return true;
  }
}
