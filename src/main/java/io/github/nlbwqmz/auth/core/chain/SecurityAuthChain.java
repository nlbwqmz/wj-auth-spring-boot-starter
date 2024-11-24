package io.github.nlbwqmz.auth.core.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import io.github.nlbwqmz.auth.common.AuthInfo;
import io.github.nlbwqmz.auth.common.SubjectManager;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.SecurityConfiguration;
import io.github.nlbwqmz.auth.core.AuthLogin;
import io.github.nlbwqmz.auth.core.security.AuthTokenGenerate;
import io.github.nlbwqmz.auth.core.security.SecurityRealm;
import io.github.nlbwqmz.auth.core.security.configuration.AuthHandlerEntity;
import io.github.nlbwqmz.auth.core.security.configuration.Logical;
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
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
  private final SecurityRealm securityRealm;
  private final AuthLogin authLogin;
  private final List<AuthHandlerEntity> handlers = new ArrayList<>();
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  public SecurityAuthChain(AuthAutoConfiguration authAutoConfiguration,
      AuthTokenGenerate authTokenGenerate,
      SecurityRealm securityRealm,
      AuthLogin authLogin) {
    this.securityConfiguration = authAutoConfiguration.getSecurity();
    this.authTokenGenerate = authTokenGenerate;
    this.securityRealm = securityRealm;
    this.authLogin = authLogin;
  }

  @Override
  public void doFilter(ChainManager chain) {
    HttpServletRequest request = SubjectManager.getRequest();
    HttpServletResponse response = SubjectManager.getResponse();
    HandlerHelper handlerHelper = getAuthHandler(request);
    if (handlerHelper != null) {
      InterceptorHandler handler = handlerHelper.getHandler();
      String[] auth = handlerHelper.getAuth();
      String authenticate = handler.authenticate(request, response, securityConfiguration.getHeader());
      if (handler.isVerifyToken() || handler.isRefreshToken()) {
        authTokenGenerate.verify(authenticate);
      }
      if (handler.isRefreshToken()) {
        authTokenGenerate.decode(authenticate);
        String subject = SubjectManager.getSubject();
        long expire = SubjectManager.getExpire();
        authLogin.doLogin(subject, expire);
      }
      if (handler.isAuthorize() && !handler
          .authorize(request, response, auth, handlerHelper.getLogical(),
              securityRealm.doAuthorization())) {
        throw new PermissionNotFoundException(
            String.format("%s permission required, logical is %s.", ArrayUtil.join(auth, ","),
                handlerHelper.getLogical().name()));
      }
    }
    chain.doAuth();
  }

  private HandlerHelper getAuthHandler(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String method = request.getMethod();
    for (AuthHandlerEntity authHandlerEntity : handlers) {
      Set<AuthInfo> authInfos = authHandlerEntity.getAuthHelpers();
      for (AuthInfo authInfo : authInfos) {
        if (MatchUtils.matcher(authInfo, uri, method)) {
          return new HandlerHelper(authInfo.getAuth(), authInfo.getLogical(),
              authHandlerEntity.getHandler());
        }
      }
    }
    if (securityConfiguration.isStrict()) {
      return new HandlerHelper(new AuthenticateInterceptorHandler());
    } else {
      return null;
    }
  }

  public void setAuth(Set<AuthInfo> authSet) {
    Set<AuthInfo> authInfoSet = securityRealm.addAuthPatterns();
    if (CollUtil.isNotEmpty(authInfoSet)) {
      for (AuthInfo authInfo : authInfoSet) {
        Set<String> patterns = authInfo.getPatterns();
        String[] auth = authInfo.getAuth();
        if (AuthCommonUtil.isAllNotBlank(patterns) && AuthCommonUtil.isAllNotBlank(auth)) {
          authInfo.setPatterns(AuthCommonUtil.addUrlPrefix(patterns, contextPath));
          authSet.add(authInfo);
        } else {
          String clazz = securityRealm.getClass().toString();
          clazz = clazz.substring(6, clazz.indexOf("$$"));
          throw new AuthInitException(
              String
                  .format("at %s.addAuthPatterns, neither patterns nor auth can be blank.", clazz));
        }
      }
    }
    addHandler(new AuthHandlerEntity(authSet, new AuthorizeInterceptorHandler(), 0));
  }

  public void setAnon(Set<AuthInfo> anonSet) {
    if (CollUtil.isNotEmpty(securityConfiguration.getAnon())) {
      anonSet.add(AuthInfo.builder()
          .patterns(AuthCommonUtil.addUrlPrefix(securityConfiguration.getAnon(), contextPath))
          .build());
    }
    Set<AuthInfo> anonAuthInfoSet = securityRealm.addAnonPatterns();
    if (CollUtil.isNotEmpty(anonAuthInfoSet)) {
      for (AuthInfo authInfo : anonAuthInfoSet) {
        if (authInfo != null) {
          Set<String> patterns = authInfo.getPatterns();
          if (CollUtil.isNotEmpty(patterns)) {
            authInfo.setPatterns(AuthCommonUtil.addUrlPrefix(patterns, contextPath));
            anonSet.add(authInfo);
          } else {
            String clazz = securityRealm.getClass().toString();
            clazz = clazz.substring(6, clazz.indexOf("$$"));
            throw new AuthInitException(
                String.format("at %s.addAnonPatterns, patterns can't be blank.", clazz));
          }
        }
      }
    }
    addHandler(new AuthHandlerEntity(anonSet, new AnonymizeInterceptorHandler(), 100));
  }

  public void setAuthc(Set<AuthInfo> authcSet) {
    addHandler(new AuthHandlerEntity(authcSet, new AuthenticateInterceptorHandler(), 200));
  }

  public void setCustomHandler() {
    Set<AuthHandlerEntity> customHandler = securityRealm.addCustomHandler();
    if (CollUtil.isNotEmpty(customHandler)) {
      for (AuthHandlerEntity authHandlerEntity : customHandler) {
        addHandler(authHandlerEntity);
      }
    }
    this.handlers.sort(Comparator.comparingInt(AuthHandlerEntity::getOrder));
  }

  private void addHandler(AuthHandlerEntity authHandlerEntity) {
    this.handlers.add(authHandlerEntity);
  }

  private static class HandlerHelper {

    private String[] auth;
    private Logical logical;
    private InterceptorHandler handler;

    public HandlerHelper(InterceptorHandler handler) {
      this.handler = handler;
    }

    public HandlerHelper(String[] auth, Logical logical, InterceptorHandler handler) {
      this.auth = auth;
      this.logical = logical;
      this.handler = handler;
    }

    public String[] getAuth() {
      return auth;
    }

    public void setAuth(String[] auth) {
      this.auth = auth;
    }

    public Logical getLogical() {
      return logical;
    }

    public void setLogical(Logical logical) {
      this.logical = logical;
    }

    public InterceptorHandler getHandler() {
      return handler;
    }

    public void setHandler(InterceptorHandler handler) {
      this.handler = handler;
    }
  }
}
