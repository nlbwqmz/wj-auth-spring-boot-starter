package io.github.nlbwqmz.auth.core.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.RateLimiter;
import io.github.nlbwqmz.auth.common.AuthThreadLocal;
import io.github.nlbwqmz.auth.common.FilterRange;
import io.github.nlbwqmz.auth.common.SecurityInfo;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration;
import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration.Strategy;
import io.github.nlbwqmz.auth.core.rateLimiter.RateLimiterCondition;
import io.github.nlbwqmz.auth.exception.rate.RateLimiterException;
import io.github.nlbwqmz.auth.utils.AuthCommonUtil;
import io.github.nlbwqmz.auth.utils.MatchUtils;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Order(1)
@Component
public class RateLimiterAuthChain implements AuthChain {

  private final RateLimiterConfiguration rateLimiterConfiguration;
  private final RateLimiter rateLimiter;
  private final LoadingCache<String, RateLimiter> cache;
  private final RateLimiterCondition rateLimiterCondition;
  @Value("${server.servlet.context-path:}")
  private String contextPath;
  private ImmutableSet<SecurityInfo> ignored;
  private ImmutableSet<SecurityInfo> only;

  private final Duration timeout = Duration.ofMillis(100);

  public RateLimiterAuthChain(AuthAutoConfiguration authAutoConfiguration,
      @Autowired(required = false) RateLimiterCondition rateLimiterCondition) {
    this.rateLimiterConfiguration = authAutoConfiguration.getRateLimiter();
    this.rateLimiterCondition = rateLimiterCondition;
    if (rateLimiterConfiguration.getStrategy() == Strategy.NORMAL) {
      this.cache = null;
      this.rateLimiter = RateLimiter.create(rateLimiterConfiguration.getThreshold());
    } else {
      this.rateLimiter = null;
      this.cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
          .build(new CacheLoader<String, RateLimiter>() {
            @Override
            public RateLimiter load(String key) throws Exception {
              return RateLimiter.create(rateLimiterConfiguration.getThreshold());
            }
          });
    }

  }

  public void setRateLimiter(Set<SecurityInfo> rateLimiterSet,
      Set<SecurityInfo> rateLimiterIgnoredSet) {
    Set<String> only = rateLimiterConfiguration.getOnly();
    Set<String> ignored = rateLimiterConfiguration.getIgnored();
    if (CollUtil.isNotEmpty(only)) {
      rateLimiterSet.add(SecurityInfo.builder()
          .patterns(AuthCommonUtil.addUrlPrefix(only, contextPath)).build());
    }
    if (CollUtil.isNotEmpty(ignored)) {
      rateLimiterIgnoredSet.add(SecurityInfo.builder()
          .patterns(AuthCommonUtil.addUrlPrefix(ignored, contextPath)).build());
    }
    this.only = ImmutableSet.copyOf(rateLimiterSet);
    this.ignored = ImmutableSet.copyOf(rateLimiterIgnoredSet);
  }

  @Override
  public void doFilter(ChainManager chain) {
    if (rateLimiterConfiguration.getEnable() && checkIsLimit()) {
      switch (rateLimiterConfiguration.getStrategy()) {
        case NORMAL:
          normal();
          break;
        case IP:
          condition(getIp());
          break;
        case CUSTOM:
          condition(rateLimiterCondition
              .getCondition(AuthThreadLocal.getRequest(), AuthThreadLocal.getSubject()));
          break;
        default:
          throw new RateLimiterException("The rate limiter configuration strategy cannot match.");
      }
    }
    chain.doAuth();
  }

  private boolean checkIsLimit() {
    String uri = AuthThreadLocal.getRequest().getRequestURI();
    String method = AuthThreadLocal.getRequest().getMethod();
    FilterRange defaultFilterRange = rateLimiterConfiguration.getDefaultFilterRange();
    switch (defaultFilterRange) {
      case ALL:
        return !MatchUtils.matcher(ignored, uri, method);
      case NONE:
        return MatchUtils.matcher(only, uri, method);
      default:
        throw new RateLimiterException(
            "The rate limiter configuration defaultFilterRange cannot match.");
    }
  }

  private void normal() {
    if (!rateLimiter.tryAcquire(timeout)) {
      throw new RateLimiterException("Busy service.");
    }
  }

  private void condition(String condition) {
    try {
      if (!cache.get(condition).tryAcquire(timeout)) {
        throw new RateLimiterException("Busy service.");
      }
    } catch (ExecutionException e) {
      throw new RateLimiterException(e.getMessage());
    }
  }

  /**
   * 获取IP地址
   */
  private String getIp() {
    HttpServletRequest request = AuthThreadLocal.getRequest();
    String ipAddress = request.getHeader("x-forwarded-for");
    if (StrUtil.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("X-Real-IP");
    }
    if (StrUtil.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (StrUtil.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (StrUtil.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    if (StrUtil.isBlank(ipAddress) && ipAddress.length() > 15) {
      if (ipAddress.indexOf(",") > 0) {
        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
      }
    }
    return "0:0:0:0:0:0:0:1".equals(ipAddress) ? "127.0.0.1" : ipAddress;
  }


}
