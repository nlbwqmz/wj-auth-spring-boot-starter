package io.github.nlbwqmz.auth.core.xss;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.html.HtmlEscapers;
import io.github.nlbwqmz.auth.common.AuthServletInputStream;
import io.github.nlbwqmz.auth.utils.JacksonUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

  private final boolean queryEnable;
  private final boolean bodyEnable;

  public XssRequestWrapper(HttpServletRequest request, boolean queryEnable, boolean bodyEnable) {
    super(request);
    this.queryEnable = queryEnable;
    this.bodyEnable = bodyEnable;
  }

  @Override
  public String getParameter(String name) {
    String value = super.getParameter(name);
    if (queryEnable && StrUtil.isNotBlank(value)) {
      value = HtmlEscapers.htmlEscaper().escape(value);
    }
    return value;
  }

  @Override
  public String[] getParameterValues(String name) {
    String[] parameterValues = super.getParameterValues(name);
    if (queryEnable) {
      if (parameterValues == null) {
        return null;
      }
      for (int i = 0; i < parameterValues.length; i++) {
        String value = parameterValues[i];
        if (StrUtil.isNotBlank(value)) {
          parameterValues[i] = HtmlEscapers.htmlEscaper().escape(value);
        }
      }
    }
    return parameterValues;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    ServletInputStream servletInputStream = super.getInputStream();
    if (bodyEnable && servletInputStream.available() > 0) {
      return new AuthServletInputStream(doXss(servletInputStream).getBytes());
    } else {
      return servletInputStream;
    }
  }

  public String doXss(ServletInputStream servletInputStream) {
    String read = IoUtil.read(servletInputStream, StandardCharsets.UTF_8);
    return JacksonUtils.jsonStringDoXss(read);
  }

}
