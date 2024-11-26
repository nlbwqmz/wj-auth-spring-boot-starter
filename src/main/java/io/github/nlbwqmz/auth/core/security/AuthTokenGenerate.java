package io.github.nlbwqmz.auth.core.security;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import io.github.nlbwqmz.auth.common.AuthThreadLocal;
import io.github.nlbwqmz.auth.configuration.AuthAutoConfiguration;
import io.github.nlbwqmz.auth.configuration.TokenConfiguration;
import io.github.nlbwqmz.auth.core.security.configuration.AlgorithmEnum;
import io.github.nlbwqmz.auth.core.security.configuration.TokenKeyConfiguration;
import io.github.nlbwqmz.auth.exception.security.CertificateException;
import io.github.nlbwqmz.auth.exception.security.CertificateExpiredException;
import io.github.nlbwqmz.auth.exception.security.CertificateNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final TokenKeyConfiguration tokenKeyConfiguration;

  public AuthTokenGenerate(AuthAutoConfiguration authAutoConfiguration, @Autowired(required = false) TokenKeyConfiguration tokenKeyConfiguration) {
    this.tokenConfiguration = authAutoConfiguration.getSecurity().getToken();
    this.tokenKeyConfiguration = tokenKeyConfiguration;
  }


  /**
   * 获取签名算法
   *
   * @param isPrivate 是否为私钥
   * @return 签名算法
   */
  private JWTSigner createAlgorithm(Boolean isPrivate) {
    AlgorithmEnum algorithm = tokenConfiguration.getAlgorithm();
    String algorithmName = algorithm.name();
    if (StrUtil.startWithIgnoreCase(algorithmName, "HS")) {
      String key = tokenKeyConfiguration.key();
      Assert.notBlank(key, () -> new CertificateException("HS algorithm must set key."));
      return JWTSignerUtil.createSigner(algorithmName, key.getBytes(StandardCharsets.UTF_8));
    } else if (StrUtil.startWithIgnoreCase(algorithmName, "RS")) {
      String privateKey = tokenKeyConfiguration.privateKey();
      String publicKey = tokenKeyConfiguration.publicKey();
      Assert.isTrue(StrUtil.isNotBlank(publicKey) && StrUtil.isNotBlank(privateKey), () -> new CertificateException("RS algorithm must set public key and private key."));
      RSA rsa = SecureUtil.rsa(privateKey, publicKey);
      Key key = BooleanUtil.isTrue(isPrivate) ? rsa.getPrivateKey() : rsa.getPublicKey();
      return JWTSignerUtil.createSigner(algorithmName, key);
    }
    throw new IllegalArgumentException("Unsupported algorithms.");
  }


  /**
   * 生成token
   *
   * @param subject 载体
   * @param expire  过期时间（毫秒）
   * @return token
   */
  public String create(String subject, long expire) {
    JWT jwt = JWT.create()
        .setIssuer(tokenConfiguration.getIssuer())
        .setIssuedAt(new Date())
        .setPayload(CLAIM_SUBJECT, subject)
        .setSigner(createAlgorithm(true));
    if (expire > 0) {
      return jwt.setExpiresAt(new Date(System.currentTimeMillis() + expire)).setPayload(CLAIM_EXPIRE, expire).sign();
    }
    return jwt.sign();
  }

  /**
   * 解析
   *
   * @param token token
   */
  public void decode(String token) {
    JWT jwt = JWT.of(token).setSigner(createAlgorithm(false));
    JSONObject payloads = jwt.getPayloads();
    AuthThreadLocal.setExpireDate(payloads.getDate("exp"));
    AuthThreadLocal.setSubject(payloads.getStr(CLAIM_SUBJECT));
    AuthThreadLocal.setExpire(payloads.getLong(CLAIM_EXPIRE, 0L));
  }

  /**
   * 校验token
   *
   * @param token token
   */
  public void verify(String token) {
    Assert.notBlank(token, CertificateNotFoundException::new);
    JWTValidator validator;
    try {
      validator = JWTValidator.of(token);
    } catch (Exception e) {
      throw new CertificateException(e.getMessage());
    }
    try {
      validator.validateAlgorithm(createAlgorithm(false));
    } catch (Exception e) {
      throw new CertificateException(e.getMessage());
    }
    try {
      validator.validateDate();
    } catch (Exception e) {
      throw new CertificateExpiredException(e.getMessage());
    }
  }
}
