package io.github.nlbwqmz.auth.core;

import com.google.common.collect.Sets;
import io.github.nlbwqmz.auth.annotation.rateLimiter.RateLimit;
import io.github.nlbwqmz.auth.annotation.rateLimiter.RateLimitIgnored;
import io.github.nlbwqmz.auth.annotation.security.Anonymous;
import io.github.nlbwqmz.auth.annotation.security.Permission;
import io.github.nlbwqmz.auth.annotation.xss.Xss;
import io.github.nlbwqmz.auth.annotation.xss.XssIgnored;
import io.github.nlbwqmz.auth.common.FilterRange;
import io.github.nlbwqmz.auth.common.SecurityInfo;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration;
import io.github.nlbwqmz.auth.configuration.SecurityConfiguration;
import io.github.nlbwqmz.auth.configuration.XssConfiguration;
import io.github.nlbwqmz.auth.core.chain.RateLimiterAuthChain;
import io.github.nlbwqmz.auth.core.chain.SecurityAuthChain;
import io.github.nlbwqmz.auth.core.chain.XssAuthChain;
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
@ServletComponentScan("io.github.nlbwqmz.auth")
@ComponentScan("io.github.nlbwqmz.auth")
public class AuthInit implements ApplicationRunner {

  private final RequestMappingHandlerMapping mapping;
  private final AuthAutoConfiguration authAutoConfiguration;
  private final SecurityAuthChain securityChain;
  private final XssAuthChain xssChain;
  private final RateLimiterAuthChain rateLimiterChain;
  Set<SecurityInfo> authorizeSet = Sets.newHashSet();
  Set<SecurityInfo> anonymousSet = Sets.newHashSet();
  Set<SecurityInfo> authenticatSet = Sets.newHashSet();
  Set<SecurityInfo> xssIgnoredSet = Sets.newHashSet();
  Set<SecurityInfo> xssSet = Sets.newHashSet();
  Set<SecurityInfo> rateLimiterIgnoredSet = Sets.newHashSet();
  Set<SecurityInfo> rateLimiterSet = Sets.newHashSet();
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  public AuthInit(RequestMappingHandlerMapping mapping,
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
    securityChain.setAuthorize(authorizeSet);
    securityChain.setAnonymous(anonymousSet);
    securityChain.setAuthenticate(authenticatSet);
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
    if (rateLimiterConfiguration.getEnable()) {
      FilterRange defaultFilterRange = rateLimiterConfiguration.getDefaultFilterRange();
      if (defaultFilterRange == FilterRange.ALL && hasAnnotation(method, RateLimitIgnored.class)) {
        rateLimiterIgnoredSet
            .add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
      } else if (defaultFilterRange == FilterRange.NONE && hasAnnotation(method, RateLimit.class)) {
        rateLimiterSet
            .add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
      }
    }
  }

  private void initXss(Method method, Set<String> patterns, Set<String> methods) {
    XssConfiguration xssConfiguration = authAutoConfiguration.getXss();
    if (xssConfiguration.getQueryEnable() || xssConfiguration.getBodyEnable()) {
      FilterRange defaultFilterRange = xssConfiguration.getFilterRange();
      if (defaultFilterRange == FilterRange.ALL && hasAnnotation(method, XssIgnored.class)) {
        xssIgnoredSet
            .add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
      } else if (defaultFilterRange == FilterRange.NONE && hasAnnotation(method, Xss.class)) {
        xssSet.add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
      }
    }
  }

  private void initSecurity(Method method, Set<String> patterns, Set<String> methods) {
    SecurityConfiguration securityConfiguration = authAutoConfiguration.getSecurity();
    if (securityConfiguration.getEnableAnnotation()) {
      Permission permission = method.getAnnotation(Permission.class);
      Anonymous anonymous = method.getAnnotation(Anonymous.class);
      Class<?> declaringClass = method.getDeclaringClass();
      if (permission != null) {
        String[] authValueArray = permission.value();
        if (AuthCommonUtil.isAllNotBlank(authValueArray)) {
          authorizeSet.add(
              SecurityInfo.builder()
                  .patterns(patterns)
                  .methods(methods)
                  .auth(permission.value())
                  .logical(permission.logical()).build());
        } else {
          throw new AuthInitException(
              String.format("at %s.%s, annotation Auth value can't be blank",
                  declaringClass.toString().substring(6), method.getName()));
        }
        return;
      } else if (anonymous != null) {
        anonymousSet.add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
        return;
      }
      Permission declaredPermission = declaringClass.getAnnotation(Permission.class);
      Anonymous declaredAnonymous = declaringClass.getAnnotation(Anonymous.class);
      if (declaredPermission != null) {
        authorizeSet.add(SecurityInfo.builder()
            .patterns(patterns)
            .methods(methods)
            .auth(declaredPermission.value())
            .logical(declaredPermission.logical()).build());
        return;
      } else if (declaredAnonymous != null) {
        anonymousSet.add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
        return;
      }
    }
    authenticatSet.add(SecurityInfo.builder().patterns(patterns).methods(methods).build());
  }

  private boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
    return method.isAnnotationPresent(annotationClass)
        || method.getDeclaringClass().isAnnotationPresent(annotationClass);
  }
}
