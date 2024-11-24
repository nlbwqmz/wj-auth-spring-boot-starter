package io.github.nlbwqmz.auth.configuration;

import io.github.nlbwqmz.auth.common.FilterRange;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * 限流配置
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Setter
@Getter
public class RateLimiterConfiguration {

  /**
   * 开启
   */
  private boolean enabled = false;
  /**
   * 阈值
   */
  private double threshold = 100;

  /**
   * 忽略
   */
  private Set<String> ignored;

  /**
   * 只有这些接口才限流
   */
  private Set<String> only;

  /**
   * 策略
   */
  private Strategy strategy = Strategy.NORMAL;

  /**
   * 默认过滤范围
   */
  private FilterRange defaultFilterRange = FilterRange.ALL;


  public enum Strategy {
    /**
     * 正常：全局限流
     */
    NORMAL,
    /**
     * IP：IP限流
     */
    IP,
    /**
     * 自定义
     */
    CUSTOM
  }
}
