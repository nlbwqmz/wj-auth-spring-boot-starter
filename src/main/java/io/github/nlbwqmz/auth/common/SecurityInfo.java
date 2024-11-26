package io.github.nlbwqmz.auth.common;

import io.github.nlbwqmz.auth.core.security.configuration.Logical;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求验证
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityInfo {

  private Set<String> patterns;
  private Set<String> methods;
  private String[] permission;
  private Logical logical;

}
