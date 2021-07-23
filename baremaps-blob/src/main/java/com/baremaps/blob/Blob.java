package com.baremaps.blob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Blob {

  private Long contentLength;

  private String contentType;

  private String contentEncoding;

  private InputStream inputStream;

  private Blob() {

  }

  public Long getContentLength() {
    return contentLength;
  }

  public String getContentType() {
    return contentType;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final Blob blob = new Blob();

    private Builder() {}

    public Builder withContentLength(Long contentLength) {
      blob.contentLength = contentLength;
      return this;
    }

    public Builder withContentType(String contentType) {
      blob.contentType = contentType;
      return this;
    }

    public Builder withContentEncoding(String contentEncoding) {
      blob.contentEncoding = contentEncoding;
      return this;
    }

    public Builder withInputStream(InputStream inputStream) {
      blob.inputStream = inputStream;
      return this;
    }

    public Builder withByteArray(byte[] bytes) {
      blob.contentLength = (long) bytes.length;
      blob.inputStream = new ByteArrayInputStream(bytes);
      return this;
    }

    public Blob build() {
      return blob;
    }
  }

}
