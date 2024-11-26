package io.github.nlbwqmz.auth.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import org.springframework.lang.NonNull;

public class AuthServletInputStream extends ServletInputStream {

  private final ByteArrayInputStream byteArrayInputStream;
  private boolean isFinished = false;

  public AuthServletInputStream(byte[] body) {
    this.byteArrayInputStream = new ByteArrayInputStream(body);
  }

  @Override
  public int read() throws IOException {
    int byteRead = byteArrayInputStream.read();
    if (byteRead == -1) {
      isFinished = true;
    }
    return byteRead;
  }

  @Override
  public int read(@NonNull byte[] b) throws IOException {
    int bytesRead = byteArrayInputStream.read(b);
    if (bytesRead == -1) {
      isFinished = true;
    }
    return bytesRead;
  }


  @Override
  public void close() throws IOException {
    byteArrayInputStream.close();
  }

  @Override
  public boolean isFinished() {
    return isFinished;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {

  }
}
