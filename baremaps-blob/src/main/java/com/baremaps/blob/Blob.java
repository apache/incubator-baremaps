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

/** A binary large object (blob). */
public class Blob {

  private Long contentLength;

  private String contentType;

  private String contentEncoding;

  private InputStream inputStream;

  private Blob() {}

  /**
   * Returns the content length.
   *
   * @return the content length
   */
  public Long getContentLength() {
    return contentLength;
  }

  /**
   * Returns the content type.
   *
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Returns the content encoding.
   *
   * @return the content encoding
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Returns the content.
   *
   * @return the content
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * Creates a mutable builder for blobs.
   *
   * @return a builder for blobs
   */
  public static Builder builder() {
    return new Builder();
  }

  /** A mutable builder for binary large objects (blobs). */
  public static class Builder {

    private final Blob blob = new Blob();

    private Builder() {}

    /**
     * Sets the content length.
     *
     * @param contentLength the content length
     * @return the builder
     */
    public Builder withContentLength(Long contentLength) {
      blob.contentLength = contentLength;
      return this;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the content type
     * @return the builder
     */
    public Builder withContentType(String contentType) {
      blob.contentType = contentType;
      return this;
    }

    /**
     * Sets the content encoding.
     *
     * @param contentEncoding the content encoding
     * @return the builder
     */
    public Builder withContentEncoding(String contentEncoding) {
      blob.contentEncoding = contentEncoding;
      return this;
    }

    /**
     * Sets the content.
     *
     * @param inputStream the content
     * @return the builder
     */
    public Builder withInputStream(InputStream inputStream) {
      blob.inputStream = inputStream;
      return this;
    }

    /**
     * Sets the content.
     *
     * @param bytes the content
     * @return the builder
     */
    public Builder withByteArray(byte[] bytes) {
      blob.contentLength = (long) bytes.length;
      blob.inputStream = new ByteArrayInputStream(bytes);
      return this;
    }

    /**
     * Builds an immutable blob.
     *
     * @return the blob
     */
    public Blob build() {
      return blob;
    }
  }
}
