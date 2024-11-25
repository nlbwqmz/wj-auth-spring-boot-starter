package io.github.nlbwqmz.auth.configuration;

import lombok.Data;

/**
 * token配置
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Data
public class TokenConfiguration {

  /**
   * 密码
   */
  private String password = "nlbwqmz.github.io";
  /**
   * 发行人
   */
  private String issuer = "nlbwqmz.github.io";

  /**
   * 非匿名的情况下是否刷新token
   */
  private Boolean refresh = true;

  /**
   * token有有效期少于剩余持续时长（毫秒）时执行token刷新 小于等于0时总是刷新
   */
  private Long residualDuration = 0L;


}
