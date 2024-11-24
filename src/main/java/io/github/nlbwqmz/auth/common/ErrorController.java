package io.github.nlbwqmz.auth.common;

import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.core.security.SecurityRealm;
import io.github.nlbwqmz.auth.exception.AuthException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于异常抛出，便于全局异常拦截
 *
 * @author 魏杰
 * @since 0.0.1
 */
@ConditionalOnBean(SecurityRealm.class)
@RestController
@RequestMapping("auth")
public class ErrorController {

  @RequestMapping("error")
  public void error(HttpServletRequest request) {
    RuntimeException error = (RuntimeException) request
        .getAttribute(AuthAutoConfiguration.ERROR_ATTRIBUTE);
    request.removeAttribute("authError");
    if (error instanceof AuthException) {
      throw error;
    } else {
      throw new AuthException(
          String.format("%s: %s", error.getClass().toString().substring(6), error.getMessage()));
    }
  }
}
