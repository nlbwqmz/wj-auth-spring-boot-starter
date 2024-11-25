package io.github.nlbwqmz.auth.core.security.configuration;

import io.github.nlbwqmz.auth.common.SecurityInfo;
import io.github.nlbwqmz.auth.core.security.handler.InterceptorHandler;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityHandler {

  private Set<SecurityInfo> securityInfoSet;
  private InterceptorHandler handler;
  private int order;
}
