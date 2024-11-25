package io.github.nlbwqmz.auth.utils;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import io.github.nlbwqmz.auth.common.SecurityInfo;
import java.util.Optional;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class MatchUtils {

  private final static AntPathMatcher antPathMatcher = new AntPathMatcher();

  public static boolean matcher(@NonNull Set<String> patterns, String uri) {
    for (String pattern : patterns) {
      if (antPathMatcher.match(pattern, uri)) {
        return true;
      }
    }
    return false;
  }

  public static boolean matcher(@NonNull SecurityInfo securityInfo, String uri,
      String method) {
    Set<String> patterns = Optional.ofNullable(securityInfo.getPatterns()).orElse(
        Sets.newHashSet());
    Set<String> methods = Optional.ofNullable(securityInfo.getMethods()).orElse(
        Sets.newHashSet());
    return matcher(patterns, uri) && (CollUtil.isEmpty(methods) || AuthCommonUtil
        .containsIgnoreCase(methods, method));
  }

  public static boolean matcher(@NonNull Set<SecurityInfo> set, String uri,
      String method) {
    if (CollUtil.isNotEmpty(set)) {
      for (SecurityInfo item : set) {
        if (matcher(item, uri, method)) {
          return true;
        }
      }
    }
    return false;
  }
}
