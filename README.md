# wj-auth-spring-boot-starter

基于springboot的认证授权框架，额外支持CORS、XSS、限流。

## 一、使用

```xml

<dependency>
  <groupId>io.github.nlbwqmz</groupId>
  <artifactId>wj-auth-spring-boot-starter</artifactId>
  <version>0.0.3</version>
</dependency>
```

将@EnableAuth注解加载启动类上。

```java

@EnableAuth
@SpringBootApplication
public class AuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthApplication.class, args);
  }

}
```

## 二、配置文件

**所有的路由都不用加上`context-path`前缀。**。

```yml
wj-auth:
  # 认证授权配置
  security:
    # 是否开启权限验证 默认为true
    enable: true
    # 验证token的请求头名称 默认为Authorization
    header: Authorization
    # 匿名接口
    anonymize: /anonymize/**,/login
    # 严格模式 默认为true
    # true:所有请求都会被过滤，被springboot扫描到的请求按照设置过滤，未被扫描到的请求将进行认证校验但不会进行授权校验
    # false:只有被springboot扫描到的请求会被过滤
    strict: true
    # 是否开启注解扫描 默认为true
    enable-annotation: true
    # token配置
    token:
      # 签名算法 默认为HS256
      # 支持HS256,HS384,HS512,RS256,RS384,RS512
      algorithm: HS256
      # token发行人 默认为nlbwqmz.github.io
      issuer: nlbwqmz.github.io
      # 非匿名的情况下是否刷新token 默认为true
      refresh: true
      # token有效期少于剩余持续时长（毫秒）时执行token刷新 小于等于0且当前token存在有效期时总是刷新
      residual-duration: 0
  # xss配置
  xss:
    # 是否开启query过滤 默认为false
    query-enable: true
    # 是否开启body过滤 默认为false
    body-enable: false
    # 过滤范围 默认为all
    # all全部过滤 （@XssIgnored注解和ignored配置的路由仍然会生效）
    # none 全部不过滤 （@Xss注解仍然会生效）
    filter-range: all
    # 忽略过滤
    ignored: /ignoredXss/**,/ignoredXss2/**
    # 仅过滤路由
    # 若only不为空，则只有only中所包含的路由才进行XSS过滤，ignored将失效
    only: /route/**
  # 跨域配置
  cors:
    # 是否开启跨域 默认false
    enabled: true
    access-control-allow-credentials: false
    access-control-allow-headers: *
    access-control-allow-methods: PUT, POST, GET, DELETE, OPTIONS
    access-control-allow-origin: *
    access-control-max-age: 3600
  # 限流配置
  rate-limiter:
    # 是否开启限流 默认为false
    enable: false
    # 阈值(QPS) 默认为100
    threshold: 100
    # 限流范围 默认为all
    # all全部限流 （@RateLimitIgnored注解的接口和ignored配置的路由仍然会生效）
    # none 全部不过滤 （@RateLimit注解仍然会生效）
    default-filter-range: all
    # 忽略限流
    ignored: /ignoredRateLimiter/**
    # 仅限流路由
    # 若only不为空，则只有only中所包含的路由才进行限流，ignored将失效
    only: /route/**
    # 限流策略
    # normal:默认策略 全局限流
    # ip: ip限流
    # custom: 自定义限流 （需注册RateLimiterCondition.class Bean）
    strategy: normal
```

## 三、配置

***1. 必须实现`AuthRealm`接口。***

```java
public interface AuthRealm {

  /**
   * 用户授权
   *
   * @return 用户权限集合
   */
  default Set<String> userPermission(String subject) {
    return null;
  }

  /**
   * 添加 匿名 Patterns
   *
   * @return 匿名 Patterns 集合
   */
  default Set<SecurityInfo> anonymous() {
    return null;
  }

  /**
   * 添加 授权 Patterns
   *
   * @return 权限验证 Patterns 集合
   */
  default Set<SecurityInfo> authorize() {
    return null;
  }

  /**
   * 添加自定义拦截器
   *
   * @return 自定义拦截器集合
   */
  default Set<SecurityHandler> customSecurityHandler() {
    return null;
  }

  /**
   * 异常处理
   *
   * @param request   请求信息
   * @param response  响应信息
   * @param throwable 异常信息
   */
  void handleError(HttpServletRequest request, HttpServletResponse response, Throwable throwable);
}
```

**2. 若开启认证授权，必须实现`TokenKeyConfiguration`接口。**

```java
public interface TokenKeyConfiguration {

  /**
   * HS 算法密钥
   */
  default String key() {
    return null;
  }

  /**
   * RS 算法公钥
   */
  default String publicKey() {
    return null;
  }

  /**
   * RS 算法私钥
   */
  default String privateKey() {
    return null;
  }

}
```

**3. 若使用自定义限流策略，必须实现`RateLimiterCondition`接口。**

```java
public interface RateLimiterCondition {

  /**
   * 获取限流条件
   *
   * @param request 请求信息
   * @param subject token载体
   * @return 限流条件
   */
  String getCondition(HttpServletRequest request, String subject);
}
```

## 四、注解说明

所有接口都可注解在**controller接口或类**上，同时存在将采取**就近原则**。

### `@Permission`

标识接口访问所需**权限**。

#### 参数

- value: 所需权限
- logical: 多权限检查逻辑(and、or)

### `@Anonymous`

标识接口可**匿名**访问。

### `@Xss`

标识接口需进行**xss过滤**。

### `@XssIgnored`

标识接口将忽略**xss过滤**。

### `@RateLimit`

标识接口需进行**限流**。

### `@RateLimitIgnored`

标识接口将**不进行限流**。

## 五、登录及用户信息

- 登录

调用成功后，会将token写入`response`响应头中。

```java
String subject = "userId";

// 签发永不过期的token
AuthManager.

login(subject);

// 签发60分钟的token
AuthManager.

login(subject, 60*60*1000);

// 签发60分钟的token
AuthManager.

login(subject, 60,TimeUnit.MINUTES);
```

- 获取用户信息

在**匿名**接口中，subject**可能为空**，若token存在，也会尝试解析token。

```java
String subject = AuthManager.getSubject();
```

## 六、其他

- 在认证授权过程中，会在请求头中获取token。