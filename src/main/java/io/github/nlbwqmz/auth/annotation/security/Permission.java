package io.github.nlbwqmz.auth.annotation.security;

import io.github.nlbwqmz.auth.core.security.configuration.Logical;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

  /**
   * 权限
   */
  String[] value();

  /**
   * 多权限检查逻辑
   */
  Logical logical() default Logical.AND;
}
