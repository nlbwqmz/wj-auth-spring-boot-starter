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


}
