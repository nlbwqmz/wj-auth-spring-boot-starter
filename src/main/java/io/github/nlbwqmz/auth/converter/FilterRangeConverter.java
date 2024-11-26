package io.github.nlbwqmz.auth.converter;

import io.github.nlbwqmz.auth.common.FilterRange;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Configuration
public class FilterRangeConverter implements Converter<String, FilterRange> {

  @Override
  public FilterRange convert(@NonNull String source) {
    return FilterRange.valueOf(source);
  }
}
