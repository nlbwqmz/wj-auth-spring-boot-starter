package io.github.nlbwqmz.auth.core.chain;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.ImmutableSet;
import io.github.nlbwqmz.auth.common.AuthInfo;
import io.github.nlbwqmz.auth.common.FilterRange;
import io.github.nlbwqmz.auth.common.SubjectManager;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.XssConfiguration;
import io.github.nlbwqmz.auth.core.xss.XssRequestWrapper;
import io.github.nlbwqmz.auth.exception.xss.XssException;
import io.github.nlbwqmz.auth.utils.AuthCommonUtil;
import io.github.nlbwqmz.auth.utils.MatchUtils;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Order(2)
@Component
public class XssAuthChain implements AuthChain {

  private final XssConfiguration xssConfiguration;
  private ImmutableSet<AuthInfo> xssIgnored;
  private ImmutableSet<AuthInfo> xssOnly;
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  public XssAuthChain(AuthAutoConfiguration authAutoConfiguration) {
    this.xssConfiguration = authAutoConfiguration.getXss();
  }

  public void setXss(Set<AuthInfo> xssSet, Set<AuthInfo> xssIgnoredSet) {
    Set<String> only = xssConfiguration.getOnly();
    Set<String> ignored = xssConfiguration.getIgnored();
    if (CollUtil.isNotEmpty(only)) {
      xssSet.add(
          AuthInfo.builder().patterns(AuthCommonUtil.addUrlPrefix(only, contextPath))
              .build());
    }
    if (CollUtil.isNotEmpty(ignored)) {
      xssIgnoredSet.add(AuthInfo.builder()
          .patterns(AuthCommonUtil.addUrlPrefix(ignored, contextPath)).build());
    }
    xssOnly = ImmutableSet.copyOf(xssSet);
    xssIgnored = ImmutableSet.copyOf(xssIgnoredSet);
  }

  @Override
  public void doFilter(ChainManager chain) {
    HttpServletRequest request = SubjectManager.getRequest();
    if (isDoXss(request)) {
      SubjectManager.setRequest(new XssRequestWrapper(request, xssConfiguration.isQueryEnable(),
          xssConfiguration.isBodyEnable()));
    }
    chain.doAuth();
  }

  /**
   * 是否执行xss过滤
   *
   * @param request 请求
   */
  private boolean isDoXss(HttpServletRequest request) {
    if (request != null) {
      FilterRange defaultFilterRange = xssConfiguration.getFilterRange();
      String uri = request.getRequestURI();
      String method = request.getMethod();
      switch (defaultFilterRange) {
        case ALL:
          return !MatchUtils.matcher(xssIgnored, uri, method);
        case NONE:
          return MatchUtils.matcher(xssOnly, request.getRequestURI(), request.getMethod());
        default:
          throw new XssException("xss configuration defaultFilterRange cannot match");
      }
    } else {
      return false;
    }
  }
}
