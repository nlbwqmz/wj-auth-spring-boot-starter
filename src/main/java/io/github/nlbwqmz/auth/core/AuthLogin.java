package io.github.nlbwqmz.auth.core;

import io.github.nlbwqmz.auth.common.SubjectManager;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.SecurityConfiguration;
import io.github.nlbwqmz.auth.core.security.AuthTokenGenerate;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Component
public class AuthLogin {

  private final SecurityConfiguration security;
  private final AuthTokenGenerate authTokenGenerate;

  public AuthLogin(AuthAutoConfiguration authAutoConfiguration,
      AuthTokenGenerate authTokenGenerate) {
    this.security = authAutoConfiguration.getSecurity();
    this.authTokenGenerate = authTokenGenerate;
  }

  /**
   * 登录
   *
   * @param subject  载体
   * @param expire   过期时长
   * @param timeUnit 过期时长单位
   */
  public void doLogin(String subject, long expire, TimeUnit timeUnit) {
    doLogin(subject, timeUnit.toMillis(expire));
  }

  /**
   * 登录
   *
   * @param subject 载体
   * @param expire  过期时长（毫秒）
   */
  public void doLogin(String subject, long expire) {
    HttpServletResponse response = SubjectManager.getResponse();
    response.setHeader(security.getHeader(),
        authTokenGenerate.create(subject, expire));
    response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, security.getHeader());
  }

}
