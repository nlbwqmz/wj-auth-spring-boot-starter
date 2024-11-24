package io.github.nlbwqmz.auth.core.security.configuration;

import io.github.nlbwqmz.auth.common.AuthInfo;
import io.github.nlbwqmz.auth.core.security.handler.InterceptorHandler;
import java.util.Set;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class AuthHandlerEntity {

  private Set<AuthInfo> authInfos;
  private InterceptorHandler handler;
  private int order;

  public AuthHandlerEntity() {
  }

  public AuthHandlerEntity(Set<AuthInfo> authInfos,
      InterceptorHandler handler, int order) {
    this.authInfos = authInfos;
    this.handler = handler;
    this.order = order;
  }

  public Set<AuthInfo> getAuthHelpers() {
    return authInfos;
  }

  public AuthHandlerEntity setAuthHelpers(
      Set<AuthInfo> authInfos) {
    this.authInfos = authInfos;
    return this;
  }

  public InterceptorHandler getHandler() {
    return handler;
  }

  public AuthHandlerEntity setHandler(InterceptorHandler handler) {
    this.handler = handler;
    return this;
  }

  public int getOrder() {
    return order;
  }

  public AuthHandlerEntity setOrder(int order) {
    this.order = order;
    return this;
  }
}
