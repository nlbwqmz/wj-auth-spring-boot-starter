package io.github.nlbwqmz.auth.configuration;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration.Strategy;
import io.github.nlbwqmz.auth.core.AuthInit;
import io.github.nlbwqmz.auth.core.rateLimiter.RateLimiterCondition;
import io.github.nlbwqmz.auth.core.security.configuration.AlgorithmEnum;
import io.github.nlbwqmz.auth.core.security.configuration.TokenKeyConfiguration;
import io.github.nlbwqmz.auth.exception.rate.RateLimiterException;
import io.github.nlbwqmz.auth.exception.security.TokenFactoryInitException;
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
  private final TokenKeyConfiguration tokenKeyConfiguration;

  public AuthAutoConfiguration(@Autowired(required = false) RateLimiterCondition rateLimiterCondition,
      @Autowired(required = false) TokenKeyConfiguration tokenKeyConfiguration) {
    this.rateLimiterCondition = rateLimiterCondition;
    this.tokenKeyConfiguration = tokenKeyConfiguration;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    checkRateLimiterConfiguration();
    checkSecurityConfiguration();
  }

  private void checkRateLimiterConfiguration() {
    if (rateLimiter.getEnable()) {
      if (rateLimiter.getThreshold() < 1) {
        throw new RateLimiterException(
            "The minimum rate limit threshold is 1, and the default is 100.");
      }
      if (rateLimiter.getStrategy() == Strategy.CUSTOM && rateLimiterCondition == null) {
        throw new RateLimiterException(
            "The rate limiter strategy is CUSTOM,so bean RateLimiterCondition is required.");
      }
    }
  }

  private void checkSecurityConfiguration() {
    if (security.getEnable()) {
      AlgorithmEnum algorithm = security.getToken().getAlgorithm();
      Assert.notNull(algorithm, () -> new TokenFactoryInitException("Algorithm must be set."));
      Assert.notNull(tokenKeyConfiguration, () -> new TokenFactoryInitException("TokenKeyConfiguration must be set."));
      String algorithmName = algorithm.name();
      if (StrUtil.startWithIgnoreCase(algorithmName, "HS")) {
        Assert.notBlank(tokenKeyConfiguration.key(), () -> new TokenFactoryInitException("HS algorithm must set key."));
      } else if (StrUtil.startWithIgnoreCase(algorithmName, "RS")) {
        Assert.isTrue(
            StrUtil.isNotBlank(tokenKeyConfiguration.publicKey()) && StrUtil.isNotBlank(tokenKeyConfiguration.privateKey()),
            () -> new TokenFactoryInitException("RS algorithm must set public key and private key."));
      } else {
        throw new TokenFactoryInitException("Unsupported algorithms.");
      }
    }
  }

}
