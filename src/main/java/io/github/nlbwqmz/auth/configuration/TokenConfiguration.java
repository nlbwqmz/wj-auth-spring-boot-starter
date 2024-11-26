package io.github.nlbwqmz.auth.configuration;

import io.github.nlbwqmz.auth.core.security.configuration.AlgorithmEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * token配置
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Getter
@Setter
public class TokenConfiguration {

  /**
   * 发行人
   */
  private String issuer = "nlbwqmz.github.io";

  /**
   * 非匿名的情况下是否刷新token
   */
  private Boolean refresh = true;

  /**
   * token有有效期少于剩余持续时长（毫秒）时执行token刷新 小于等于0且当前token存在有效期时总是刷新
   */
  private Long residualDuration = 0L;

  /**
   * 算法
   */
  private AlgorithmEnum algorithm = AlgorithmEnum.HS256;


}
