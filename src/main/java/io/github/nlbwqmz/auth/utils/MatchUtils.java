package io.github.nlbwqmz.auth.utils;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import io.github.nlbwqmz.auth.common.AuthInfo;
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

  public static boolean matcher(@NonNull AuthInfo authInfo, String uri,
      String method) {
    Set<String> patterns = Optional.ofNullable(authInfo.getPatterns()).orElse(
        Sets.newHashSet());
    Set<String> methods = Optional.ofNullable(authInfo.getMethods()).orElse(
        Sets.newHashSet());
    return matcher(patterns, uri) && (CollUtil.isEmpty(methods) || AuthCommonUtil
        .containsIgnoreCase(methods, method));
  }

  public static boolean matcher(@NonNull Set<AuthInfo> set, String uri,
      String method) {
    if (CollUtil.isNotEmpty(set)) {
      for (AuthInfo item : set) {
        if (matcher(item, uri, method)) {
          return true;
        }
      }
    }
    return false;
  }
}
