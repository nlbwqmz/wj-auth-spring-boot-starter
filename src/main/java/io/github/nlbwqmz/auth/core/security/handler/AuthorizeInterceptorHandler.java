package io.github.nlbwqmz.auth.core.security.handler;

import cn.hutool.core.collection.CollUtil;
import io.github.nlbwqmz.auth.core.security.configuration.Logical;
import io.github.nlbwqmz.auth.exception.security.AuthSecurityException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 授权
 *
 * @author 魏杰
 * @since 0.0.1
 */
public class AuthorizeInterceptorHandler implements InterceptorHandler {

  @Override
  public boolean authorize(HttpServletRequest request, HttpServletResponse response, String[] auth,
      Logical logical,
      Set<String> userPermission) {
    if (CollUtil.isEmpty(userPermission)) {
      return false;
    }
    switch (logical) {
      case OR:
        return checkOr(userPermission, auth);
      case AND:
        return checkAnd(userPermission, auth);
      default:
        throw new AuthSecurityException("unknown exception");
    }
  }

  @Override
  public String authenticate(HttpServletRequest request, HttpServletResponse response,
      String header) {
    return request.getHeader(header);
  }

  private boolean checkOr(Set<String> userAuth, String[] auth) {
    for (String item : auth) {
      if (userAuth.contains(item)) {
        return true;
      }
    }
    return false;
  }

  private boolean checkAnd(Set<String> userAuth, String[] auth) {
    for (String item : auth) {
      if (!userAuth.contains(item)) {
        return false;
      }
    }
    return true;
  }
}
