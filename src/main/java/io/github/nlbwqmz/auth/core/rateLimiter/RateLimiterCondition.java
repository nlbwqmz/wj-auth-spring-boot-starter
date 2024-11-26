package io.github.nlbwqmz.auth.core.rateLimiter;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@FunctionalInterface
public interface RateLimiterCondition {

  /**
   * 获取限流条件
   *
   * @param request 请求信息
   * @param subject token载体
   * @return 限流条件
   */
  String getCondition(HttpServletRequest request, String subject);
}
