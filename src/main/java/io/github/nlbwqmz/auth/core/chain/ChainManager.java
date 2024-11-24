package io.github.nlbwqmz.auth.core.chain;

import java.util.List;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class ChainManager {

  private final List<AuthChain> authChains;
  private int chainIndex = 0;

  public ChainManager(List<AuthChain> authChains) {
    this.authChains = authChains;
  }

  public void doAuth() {
    if (chainIndex < authChains.size()) {
      authChains.get(chainIndex++).doFilter(this);
    }
  }
}
