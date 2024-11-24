package io.github.nlbwqmz.auth.configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * 跨域配置
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Setter
@Getter
public class CorsConfiguration {

  private boolean enabled = false;
  private String[] accessControlAllowOrigin = new String[]{"*"};
  private String[] accessControlAllowHeaders = new String[]{"*"};
  private String[] accessControlAllowMethods
      = new String[]{"PUT", "POST", "GET", "DELETE", "OPTIONS"};
  private Boolean accessControlAllowCredentials = false;
  private Long accessControlMaxAge = 3600L;
}
