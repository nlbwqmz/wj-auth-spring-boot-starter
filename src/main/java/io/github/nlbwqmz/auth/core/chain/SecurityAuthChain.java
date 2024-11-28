package io.github.nlbwqmz.auth.core.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import io.github.nlbwqmz.auth.common.AuthThreadLocal;
import io.github.nlbwqmz.auth.common.SecurityInfo;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.AuthRealm;
import io.github.nlbwqmz.auth.configuration.SecurityConfiguration;
import io.github.nlbwqmz.auth.core.AuthManager;
import io.github.nlbwqmz.auth.core.security.AuthTokenGenerate;
import io.github.nlbwqmz.auth.core.security.configuration.Logical;
import io.github.nlbwqmz.auth.core.security.configuration.SecurityHandler;
import io.github.nlbwqmz.auth.core.security.handler.AnonymizeInterceptorHandler;
import io.github.nlbwqmz.auth.core.security.handler.AuthenticateInterceptorHandler;
import io.github.nlbwqmz.auth.core.security.handler.AuthorizeInterceptorHandler;
import io.github.nlbwqmz.auth.core.security.handler.InterceptorHandler;
import io.github.nlbwqmz.auth.exception.AuthInitException;
import io.github.nlbwqmz.auth.exception.security.PermissionNotFoundException;
import io.github.nlbwqmz.auth.utils.AuthCommonUtil;
import io.github.nlbwqmz.auth.utils.MatchUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Order(0)
@Component
public class SecurityAuthChain implements AuthChain {

  private final SecurityConfiguration securityConfiguration;
  private final AuthTokenGenerate authTokenGenerate;
  private final AuthRealm authRealm;
  private final List<SecurityHandler> handlers = new ArrayList<>();
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  public SecurityAuthChain(AuthAutoConfiguration authAutoConfiguration,
      AuthTokenGenerate authTokenGenerate,
      AuthRealm authRealm) {
    this.securityConfiguration = authAutoConfiguration.getSecurity();
    this.authTokenGenerate = authTokenGenerate;
    this.authRealm = authRealm;
  }

  @Override
  public void doFilter(ChainManager chain) {
    if (BooleanUtil.isTrue(securityConfiguration.getEnable())) {
      HttpServletRequest request = AuthThreadLocal.getRequest();
      HttpServletResponse response = AuthThreadLocal.getResponse();
      HandlerInfo handlerInfo = getAuthHandler(request);
      if (Objects.nonNull(handlerInfo)) {
        InterceptorHandler handler = handlerInfo.getHandler();
        String[] auth = handlerInfo.getAuth();
        String authenticate = handler.authenticate(request, response, securityConfiguration.getHeader());
        // 验证token
        if (handler.isVerifyToken() || handler.isRefreshToken()) {
          authTokenGenerate.verify(authenticate);
          authTokenGenerate.decode(authenticate);
        } else {
          // 匿名接口也尝试解析token信息，但是会忽略异常情况
          try {
            authTokenGenerate.verify(authenticate);
            authTokenGenerate.decode(authenticate);
          } catch (Exception ignored) {
          }
        }
        // 刷新token
        if (handler.isRefreshToken() && securityConfiguration.getToken().getRefresh()) {
          Date expireDate = AuthThreadLocal.getExpireDate();
          if (Objects.nonNull(expireDate)) {
            Long residualDuration = securityConfiguration.getToken().getResidualDuration();
            if (residualDuration <= 0
                || DateUtil.between(new Date(), expireDate, DateUnit.MS, false) < residualDuration) {
              String subject = AuthThreadLocal.getSubject();
              long expire = AuthThreadLocal.getExpire();
              AuthManager.login(subject, expire);
            }
          }
        }
        if (handler.isAuthorize() && !handler.authorize(request, response, auth, handlerInfo.getLogical(),
            authRealm.userPermission(AuthThreadLocal.getSubject()))) {
          throw new PermissionNotFoundException(
              String.format("Permission [%s] required, logical is %s.", ArrayUtil.join(auth, ","),
                  handlerInfo.getLogical().name()));
        }
      }
    }
    chain.doAuth();
  }

  private HandlerInfo getAuthHandler(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String method = request.getMethod();
    for (SecurityHandler securityHandler : handlers) {
      Set<SecurityInfo> securityInfos = securityHandler.getSecurityInfoSet();
      for (SecurityInfo securityInfo : securityInfos) {
        if (MatchUtils.matcher(securityInfo, uri, method)) {
          return new HandlerInfo(securityInfo.getPermission(), securityInfo.getLogical(),
              securityHandler.getHandler());
        }
      }
    }
    if (securityConfiguration.getStrict()) {
      return new HandlerInfo(new AuthenticateInterceptorHandler());
    } else {
      return null;
    }
  }

  public void setAuthorize(Set<SecurityInfo> authSet) {
    Set<SecurityInfo> securityInfoSet = authRealm.authorize();
    if (CollUtil.isNotEmpty(securityInfoSet)) {
      for (SecurityInfo securityInfo : securityInfoSet) {
        Set<String> patterns = securityInfo.getPatterns();
        String[] auth = securityInfo.getPermission();
        if (AuthCommonUtil.isAllNotBlank(patterns) && AuthCommonUtil.isAllNotBlank(auth)) {
          securityInfo.setPatterns(AuthCommonUtil.addUrlPrefix(patterns, contextPath));
          authSet.add(securityInfo);
        } else {
          String clazz = authRealm.getClass().toString();
          clazz = clazz.substring(6, clazz.indexOf("$$"));
          throw new AuthInitException(
              String
                  .format("At %s.authorize, neither patterns nor auth can be blank.", clazz));
        }
      }
    }
    addSecurityHandler(new SecurityHandler(authSet, new AuthorizeInterceptorHandler(), 0));
  }

  public void setAnonymous(Set<SecurityInfo> anonSet) {
    if (CollUtil.isNotEmpty(securityConfiguration.getAnonymize())) {
      anonSet.add(SecurityInfo.builder()
          .patterns(AuthCommonUtil.addUrlPrefix(securityConfiguration.getAnonymize(), contextPath))
          .build());
    }
    Set<SecurityInfo> anonSecurityInfoSet = authRealm.anonymous();
    if (CollUtil.isNotEmpty(anonSecurityInfoSet)) {
      for (SecurityInfo securityInfo : anonSecurityInfoSet) {
        if (securityInfo != null) {
          Set<String> patterns = securityInfo.getPatterns();
          if (CollUtil.isNotEmpty(patterns)) {
            securityInfo.setPatterns(AuthCommonUtil.addUrlPrefix(patterns, contextPath));
            anonSet.add(securityInfo);
          } else {
            String clazz = authRealm.getClass().toString();
            clazz = clazz.substring(6, clazz.indexOf("$$"));
            throw new AuthInitException(
                String.format("At %s.anonymous, patterns can't be blank.", clazz));
          }
        }
      }
    }
    addSecurityHandler(new SecurityHandler(anonSet, new AnonymizeInterceptorHandler(), 100));
  }

  public void setAuthenticate(Set<SecurityInfo> authcSet) {
    addSecurityHandler(new SecurityHandler(authcSet, new AuthenticateInterceptorHandler(), 200));
  }

  public void setCustomHandler() {
    Set<SecurityHandler> customHandler = authRealm.customSecurityHandler();
    if (CollUtil.isNotEmpty(customHandler)) {
      for (SecurityHandler securityHandler : customHandler) {
        addSecurityHandler(securityHandler);
      }
    }
    this.handlers.sort(Comparator.comparingInt(SecurityHandler::getOrder));
  }

  private void addSecurityHandler(SecurityHandler securityHandler) {
    this.handlers.add(securityHandler);
  }


  @Getter
  @Setter
  private static class HandlerInfo {

    private String[] auth;
    private Logical logical;
    private InterceptorHandler handler;

    public HandlerInfo(InterceptorHandler handler) {
      this.handler = handler;
    }

    public HandlerInfo(String[] auth, Logical logical, InterceptorHandler handler) {
      this.auth = auth;
      this.logical = logical;
      this.handler = handler;
    }
  }
}
