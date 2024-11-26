package io.github.nlbwqmz.auth.core.security;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import io.github.nlbwqmz.auth.common.AuthThreadLocal;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.TokenConfiguration;
import io.github.nlbwqmz.auth.exception.security.CertificateException;
import io.github.nlbwqmz.auth.exception.security.CertificateExpiredException;
import io.github.nlbwqmz.auth.exception.security.CertificateNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.stereotype.Component;

/**
 * @author 魏杰
 * @since 0.0.1
 */
@Component
public class AuthTokenGenerate {

  /**
   * 载体
   */
  private final String CLAIM_SUBJECT = "sub";
  /**
   * 过期时间
   */
  private final String CLAIM_EXPIRE = "expire";

  private final TokenConfiguration tokenConfiguration;

  public AuthTokenGenerate(AuthAutoConfiguration authAutoConfiguration) {
    this.tokenConfiguration = authAutoConfiguration.getSecurity().getToken();
  }

  public String create(String subject, long expire) {
    JWT jwt = JWT.create()
        .setIssuer(tokenConfiguration.getIssuer())
        .setIssuedAt(new Date())
        .setPayload(CLAIM_SUBJECT, subject)
        .setKey(tokenConfiguration.getPassword().getBytes(StandardCharsets.UTF_8));
    if (expire > 0) {
      return jwt.setExpiresAt(new Date(System.currentTimeMillis() + expire)).setPayload(CLAIM_EXPIRE, expire).sign();
    }
    return jwt.sign();
  }

  public void decode(String token) {
    JWT jwt = JWT.of(token).setKey(tokenConfiguration.getPassword().getBytes(StandardCharsets.UTF_8));
    JSONObject payloads = jwt.getPayloads();
    AuthThreadLocal.setExpireDate(payloads.getDate("exp"));
    AuthThreadLocal.setSubject(payloads.getStr(CLAIM_SUBJECT));
    AuthThreadLocal.setExpire(payloads.getLong(CLAIM_EXPIRE, 0L));
  }

  public void verify(String token) {
    Assert.notBlank(token, CertificateNotFoundException::new);
    JWTValidator validator;
    try {
      validator = JWTValidator.of(token);
    } catch (Exception e) {
      throw new CertificateException(e.getMessage());
    }
    try {
      validator.validateAlgorithm(JWTSignerUtil.hs256(tokenConfiguration.getPassword().getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new CertificateException(e.getMessage());
    }
    try {
      validator.validateDate();
    } catch (Exception e) {
      throw new CertificateExpiredException(e.getMessage());
    }
  }

  public static void main(String[] args) {
    JWT jwt = JWT.create()
        .setIssuer("nlbwqmz.github.io")
        .setIssuedAt(new Date())
        .setPayload("sub", "user_id_test")
        .setExpiresAt(DateUtil.offsetSecond(new Date(), 60))
        .setKey("nlbwqmz.github.io".getBytes(StandardCharsets.UTF_8));
    System.out.printf(jwt.sign());
  }
}
