package io.github.nlbwqmz.auth.converter;

import io.github.nlbwqmz.auth.configuration.RateLimiterConfiguration.Strategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * 枚举注入
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Configuration
public class RateLimiterConverter implements Converter<String, Strategy> {

  @Override
  public Strategy convert(@NonNull String source) {
    return Strategy.valueOf(source);
  }
}
