package io.github.nlbwqmz.auth.configuration;

import io.github.nlbwqmz.auth.common.FilterRange;
import java.util.Set;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class XssConfiguration {

  /**
   * 开启query过滤
   */
  private boolean queryEnable = false;

  /**
   * 开启body过滤
   */
  private boolean bodyEnable = false;
  /**
   * 过滤范围
   */
  private FilterRange filterRange = FilterRange.ALL;
  /**
   * 若only为空，则ignored中包含的路由不进行XSS过滤，其他路由正常过滤 若only不为空，则ignored失效
   */
  private Set<String> ignored;

  /**
   * 若only不为空，则只有only中所包含的路由才进行XSS过滤，ignored将失效
   */
  private Set<String> only;

  public boolean isQueryEnable() {
    return queryEnable;
  }

  public void setQueryEnable(boolean queryEnable) {
    this.queryEnable = queryEnable;
  }

  public boolean isBodyEnable() {
    return bodyEnable;
  }

  public void setBodyEnable(boolean bodyEnable) {
    this.bodyEnable = bodyEnable;
  }

  public Set<String> getIgnored() {
    return ignored;
  }

  public void setIgnored(Set<String> ignored) {
    this.ignored = ignored;
  }

  public Set<String> getOnly() {
    return only;
  }

  public void setOnly(Set<String> only) {
    this.only = only;
  }

  public FilterRange getFilterRange() {
    return filterRange;
  }

  public void setFilterRange(FilterRange filterRange) {
    this.filterRange = filterRange;
  }
}
