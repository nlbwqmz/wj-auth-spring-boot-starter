package io.github.nlbwqmz.auth.configuration;

import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration.Strategy;
import io.github.nlbwqmz.auth.core.AuthInit;
import io.github.nlbwqmz.auth.core.rateLimiter.RateLimiterCondition;
import io.github.nlbwqmz.auth.exception.rate.RateLimiterException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auth 配置类
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(AuthAutoConfiguration.AUTH_PREFIX)
@Import(AuthInit.class)
public class AuthAutoConfiguration implements InitializingBean {

  public final static String AUTH_PREFIX = "wj-auth";

  /**
   * 授权认证配置
   */
  @NestedConfigurationProperty
  private SecurityConfiguration security = new SecurityConfiguration();

  /**
   * xss配置
   */
  @NestedConfigurationProperty
  private XssConfiguration xss = new XssConfiguration();
  /**
   * 跨域配置
   */
  @NestedConfigurationProperty
  private CorsConfiguration cors = new CorsConfiguration();

  /**
   * 限流配置
   */
  @NestedConfigurationProperty
  private RateLimiterConfiguration rateLimiter = new RateLimiterConfiguration();

  private final RateLimiterCondition rateLimiterCondition;

  public AuthAutoConfiguration(@Autowired(required = false) RateLimiterCondition rateLimiterCondition) {
    this.rateLimiterCondition = rateLimiterCondition;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    checkRateLimiterConfiguration();
  }

  private void checkRateLimiterConfiguration() {
    if (rateLimiter.getEnable()) {
      if (rateLimiter.getThreshold() < 1) {
        throw new RateLimiterException(
            "The minimum rate limit threshold is 1, and the default is 100");
      }
      if (rateLimiter.getStrategy() == Strategy.CUSTOM && rateLimiterCondition == null) {
        throw new RateLimiterException(
            "rate limiter strategy is CUSTOM,so bean RateLimiterCondition is required.");
      }
    }
  }

}
