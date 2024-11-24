package io.github.nlbwqmz.auth.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Strings;
import com.google.common.html.HtmlEscapers;
import io.github.nlbwqmz.auth.exception.xss.XssException;
import java.io.IOException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author 魏杰
 * @since 0.0.1
 */
public class JacksonUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  private final static ObjectMapper xssObjectMapper = new Jackson2ObjectMapperBuilder()
      .createXmlMapper(false).build();

  static {
    SimpleModule xssModule = new SimpleModule("xssSerializer");
    xssModule.addSerializer(new JsonSerializer<String>() {
      @Override
      public void serialize(String value, JsonGenerator jsonGenerator,
          SerializerProvider serializerProvider) throws IOException {
        if (!Strings.isNullOrEmpty(value)) {
          jsonGenerator.writeString(HtmlEscapers.htmlEscaper().escape(value));
        } else {
          jsonGenerator.writeString(value);
        }
      }

      @Override
      public Class<String> handledType() {
        return String.class;
      }
    });
    xssObjectMapper.registerModule(xssModule);
  }

  public static String jsonStringDoXss(String value) {
    try {
      Object obj = xssObjectMapper.readValue(value, Object.class);
      return xssObjectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new XssException(e.getMessage());
    }
  }
}
