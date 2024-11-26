package io.github.nlbwqmz.auth.configuration;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Getter
@Setter
public class SecurityConfiguration {

  /**
   * 是否开启权限验证
   */
  private Boolean enable = true;
  /**
   * Token头名称
   */
  private String header = "Authorization";
  /**
   * 免登录接口
   */
  private Set<String> anonymize;
  /**
   * 严格模式
   * true:所有请求都会被过滤，被springboot扫描到的请求按照设置过滤，未被扫描到的执行 AuthenticateInterceptorHandler
   * false:只有被springboot扫描到的请求会被过滤
   */
  private Boolean strict = true;
  /**
   * 是否开启注解
   */
  private Boolean enableAnnotation = true;

  /**
   * token配置
   */
  @NestedConfigurationProperty
  private TokenConfiguration token = new TokenConfiguration();

}
