package io.github.nlbwqmz.auth.annotation;

import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 启用
 *
 * @author 魏杰
 * @since 0.0.1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(AuthAutoConfiguration.class)
public @interface EnableAuth {

}
