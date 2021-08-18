/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.blob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Blob {

  private Long contentLength;

  private String contentType;

  private String contentEncoding;

  private InputStream inputStream;

  private Blob() {}

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
