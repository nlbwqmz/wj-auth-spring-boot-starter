package io.github.nlbwqmz.auth.core;

import cn.hutool.extra.spring.SpringUtil;
import io.github.nlbwqmz.auth.common.AuthThreadLocal;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.SecurityConfiguration;
import io.github.nlbwqmz.auth.core.security.AuthTokenGenerate;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class AuthManager {

  private static final SecurityConfiguration security;
  private static final AuthTokenGenerate authTokenGenerate;

  static {
    AuthAutoConfiguration authAutoConfiguration = SpringUtil.getBean(AuthAutoConfiguration.class);
    security = authAutoConfiguration.getSecurity();
    authTokenGenerate = SpringUtil.getBean(AuthTokenGenerate.class);
  }

  /**
   * 登录
   *
   * @param subject  载体
   * @param expire   过期时长
   * @param timeUnit 过期时长单位
   */
  public static void login(String subject, long expire, TimeUnit timeUnit) {
    login(subject, timeUnit.toMillis(expire));
  }

  /**
   * 登录
   *
   * @param subject 载体
   * @param expire  过期时长（毫秒）
   */
  public static void login(String subject, long expire) {
    HttpServletResponse response = AuthThreadLocal.getResponse();
    response.setHeader(security.getHeader(), authTokenGenerate.create(subject, expire));
    response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, security.getHeader());
  }

  /**
   * 登录
   *
   * @param subject 载体
   */
  public static void login(String subject) {
    login(subject, 0);
  }

  /**
   * 获取token载体
   *
   * @return token载体
   */
  public static String getSubject() {
    return AuthThreadLocal.getSubject();
  }

}
