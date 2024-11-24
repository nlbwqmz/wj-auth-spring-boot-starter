package io.github.nlbwqmz.auth.core;

import com.google.common.collect.Sets;
import io.github.nlbwqmz.auth.annotation.rateLimiter.RateLimit;
import io.github.nlbwqmz.auth.annotation.rateLimiter.RateLimitIgnored;
import io.github.nlbwqmz.auth.annotation.security.Anon;
import io.github.nlbwqmz.auth.annotation.security.Auth;
import io.github.nlbwqmz.auth.annotation.xss.Xss;
import io.github.nlbwqmz.auth.annotation.xss.XssIgnored;
import io.github.nlbwqmz.auth.common.AuthInfo;
import io.github.nlbwqmz.auth.common.FilterRange;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration;
import io.github.nlbwqmz.auth.configuration.SecurityConfiguration;
import io.github.nlbwqmz.auth.configuration.XssConfiguration;
import io.github.nlbwqmz.auth.core.chain.RateLimiterAuthChain;
import io.github.nlbwqmz.auth.core.chain.SecurityAuthChain;
import io.github.nlbwqmz.auth.core.chain.XssAuthChain;
import io.github.nlbwqmz.auth.core.security.SecurityRealm;
import io.github.nlbwqmz.auth.exception.AuthInitException;
import io.github.nlbwqmz.auth.utils.AuthCommonUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@ConditionalOnBean(SecurityRealm.class)
@ServletComponentScan("io.github.nlbwqmz.auth")
@ComponentScan("io.github.nlbwqmz.auth")
public class AuthRun implements ApplicationRunner {

  private final RequestMappingHandlerMapping mapping;
  private final AuthAutoConfiguration authAutoConfiguration;
  private final SecurityAuthChain securityChain;
  private final XssAuthChain xssChain;
  private final RateLimiterAuthChain rateLimiterChain;
  Set<AuthInfo> authSet = Sets.newHashSet();
  Set<AuthInfo> anonSet = Sets.newHashSet();
  Set<AuthInfo> authcSet = Sets.newHashSet();
  Set<AuthInfo> xssIgnoredSet = Sets.newHashSet();
  Set<AuthInfo> xssSet = Sets.newHashSet();
  Set<AuthInfo> rateLimiterIgnoredSet = Sets.newHashSet();
  Set<AuthInfo> rateLimiterSet = Sets.newHashSet();
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  public AuthRun(RequestMappingHandlerMapping mapping,
      AuthAutoConfiguration authAutoConfiguration,
      SecurityAuthChain securityChain,
      XssAuthChain xssChain,
      RateLimiterAuthChain rateLimiterChain) {
    this.mapping = mapping;
    this.authAutoConfiguration = authAutoConfiguration;
    this.securityChain = securityChain;
    this.xssChain = xssChain;
    this.rateLimiterChain = rateLimiterChain;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
    map.forEach((requestMappingInfo, handlerMethod) -> {
      Method method = handlerMethod.getMethod();
      Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods();
      Set<String> patterns = getPatterns(requestMappingInfo);
      Set<String> methodResult = methods.stream().map(RequestMethod::name).collect(Collectors.toSet());
      Set<String> patternResult = AuthCommonUtil.addUrlPrefix(patterns, contextPath);
      initSecurity(method, patternResult, methodResult);
      initXss(method, patternResult, methodResult);
      initRateLimiter(method, patternResult, methodResult);
    });
    securityChain.setAuth(authSet);
    securityChain.setAnon(anonSet);
    securityChain.setAuthc(authcSet);
    securityChain.setCustomHandler();
    xssChain.setXss(xssSet, xssIgnoredSet);
    rateLimiterChain.setRateLimiter(rateLimiterSet, rateLimiterIgnoredSet);
  }

  private Set<String> getPatterns(RequestMappingInfo requestMappingInfo) {
    PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
    if (Objects.nonNull(patternsCondition)) {
      return patternsCondition.getPatterns();
    }
    PathPatternsRequestCondition pathPatternsCondition = requestMappingInfo.getPathPatternsCondition();
    if (Objects.nonNull(pathPatternsCondition)) {
      return pathPatternsCondition.getPatternValues();
    }
    return null;
  }

  private void initRateLimiter(Method method, Set<String> patterns, Set<String> methods) {
    RateLimiterConfiguration rateLimiterConfiguration = authAutoConfiguration.getRateLimiter();
    if (rateLimiterConfiguration.isEnabled()) {
      FilterRange defaultFilterRange = rateLimiterConfiguration.getDefaultFilterRange();
      if (defaultFilterRange == FilterRange.ALL && hasAnnotation(method, RateLimitIgnored.class)) {
        rateLimiterIgnoredSet
            .add(AuthInfo.builder().patterns(patterns).methods(methods).build());
      } else if (defaultFilterRange == FilterRange.NONE && hasAnnotation(method, RateLimit.class)) {
        rateLimiterSet
            .add(AuthInfo.builder().patterns(patterns).methods(methods).build());
      }
    }
  }

  private void initXss(Method method, Set<String> patterns, Set<String> methods) {
    XssConfiguration xssConfiguration = authAutoConfiguration.getXss();
    if (xssConfiguration.isQueryEnable() || xssConfiguration.isBodyEnable()) {
      FilterRange defaultFilterRange = xssConfiguration.getFilterRange();
      if (defaultFilterRange == FilterRange.ALL && hasAnnotation(method, XssIgnored.class)) {
        xssIgnoredSet
            .add(AuthInfo.builder().patterns(patterns).methods(methods).build());
      } else if (defaultFilterRange == FilterRange.NONE && hasAnnotation(method, Xss.class)) {
        xssSet.add(AuthInfo.builder().patterns(patterns).methods(methods).build());
      }
    }
  }

  private void initSecurity(Method method, Set<String> patterns, Set<String> methods) {
    SecurityConfiguration securityConfiguration = authAutoConfiguration.getSecurity();
    if (securityConfiguration.isEnableAnnotation()) {
      Auth auth = method.getAnnotation(Auth.class);
      Anon anon = method.getAnnotation(Anon.class);
      Class<?> declaringClass = method.getDeclaringClass();
      if (auth != null) {
        String[] authValueArray = auth.value();
        if (AuthCommonUtil.isAllNotBlank(authValueArray)) {
          authSet.add(
              AuthInfo.builder()
                  .patterns(patterns)
                  .methods(methods)
                  .auth(auth.value())
                  .logical(auth.logical()).build());
        } else {
          throw new AuthInitException(
              String.format("at %s.%s, annotation Auth value can't be blank",
                  declaringClass.toString().substring(6), method.getName()));
        }
        return;
      } else if (anon != null) {
        anonSet.add(AuthInfo.builder().patterns(patterns).methods(methods).build());
        return;
      }
      Auth declaredAuth = declaringClass.getAnnotation(Auth.class);
      Anon declaredAnon = declaringClass.getAnnotation(Anon.class);
      if (declaredAuth != null) {
        authSet.add(AuthInfo.builder()
            .patterns(patterns)
            .methods(methods)
            .auth(declaredAuth.value())
            .logical(declaredAuth.logical()).build());
        return;
      } else if (declaredAnon != null) {
        anonSet.add(AuthInfo.builder().patterns(patterns).methods(methods).build());
        return;
      }
    }
    authcSet.add(AuthInfo.builder().patterns(patterns).methods(methods).build());
  }

  private boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
    return method.isAnnotationPresent(annotationClass)
        || method.getDeclaringClass().isAnnotationPresent(annotationClass);
  }
}
