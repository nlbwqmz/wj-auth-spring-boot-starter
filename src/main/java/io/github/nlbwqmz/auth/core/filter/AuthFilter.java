package io.github.nlbwqmz.auth.core.filter;

import io.github.nlbwqmz.auth.common.AuthThreadLocal;
import io.github.nlbwqmz.auth.core.chain.AuthChain;
import io.github.nlbwqmz.auth.core.chain.ChainManager;
import io.github.nlbwqmz.auth.core.security.SecurityRealm;
import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;

/**
 * 过滤器
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Order(0)
@WebFilter(filterName = "authFilter", urlPatterns = "/*")
@RequiredArgsConstructor
public class AuthFilter implements Filter {

  private final List<AuthChain> authChains;
  private final SecurityRealm securityRealm;


  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      AuthThreadLocal.setRequest((HttpServletRequest) request);
      AuthThreadLocal.setResponse((HttpServletResponse) response);
      new ChainManager(authChains).doAuth();
      chain.doFilter(AuthThreadLocal.getRequest(), AuthThreadLocal.getResponse());
    } catch (Throwable e) {
      securityRealm.handleError(AuthThreadLocal.getRequest(), AuthThreadLocal.getResponse(), e);
    } finally {
      AuthThreadLocal.removeAll();
    }
  }
}
