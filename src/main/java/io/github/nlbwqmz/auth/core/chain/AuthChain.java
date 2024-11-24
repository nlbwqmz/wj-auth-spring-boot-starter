package io.github.nlbwqmz.auth.core.chain;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@FunctionalInterface
public interface AuthChain {

  /**
   * 过滤
   *
   * @param chain 过滤链
   */
  void doFilter(ChainManager chain);

}
