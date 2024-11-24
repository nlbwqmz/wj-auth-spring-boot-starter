package io.github.nlbwqmz.auth.core.chain;

import cn.hutool.core.util.ArrayUtil;
import io.github.nlbwqmz.auth.common.SubjectManager;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.CorsConfiguration;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Order(Integer.MIN_VALUE)
@Component
public class CorsAuthChain implements AuthChain {

  private final CorsConfiguration corsConfiguration;

  public CorsAuthChain(AuthAutoConfiguration authAutoConfiguration) {
    corsConfiguration = authAutoConfiguration.getCors();
  }

  @Override
  public void doFilter(ChainManager chain) {
    HttpServletResponse response = SubjectManager.getResponse();
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
        corsConfiguration.getAccessControlAllowCredentials().toString()
    );
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        ArrayUtil.join(corsConfiguration.getAccessControlAllowHeaders(), ",")
    );
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        ArrayUtil.join(corsConfiguration.getAccessControlAllowMethods(), ",")
    );
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        ArrayUtil.join(corsConfiguration.getAccessControlAllowOrigin(), ",")
    );
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_MAX_AGE,
        corsConfiguration.getAccessControlMaxAge().toString()
    );
    chain.doAuth();
  }
}
