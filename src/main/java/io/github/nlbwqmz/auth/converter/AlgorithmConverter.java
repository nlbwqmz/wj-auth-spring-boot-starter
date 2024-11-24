package io.github.nlbwqmz.auth.converter;

import io.github.nlbwqmz.auth.core.security.configuration.AlgorithmEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * 枚举注入
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Configuration
public class AlgorithmConverter implements Converter<String, AlgorithmEnum> {

  @Override
  public AlgorithmEnum convert(String source) {
    return AlgorithmEnum.valueOf(source);
  }
}
