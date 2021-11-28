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

    private Long contentLength;

    private String contentType;

    private String contentEncoding;

    private InputStream inputStream;

    private byte[] byteArray;

    private Builder() {}

    /**
     * Sets the content length.
     *
     * @param contentLength the content length
     * @return the builder
     */
    public Builder withContentLength(Long contentLength) {
      this.contentLength = contentLength;
      return this;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the content type
     * @return the builder
     */
    public Builder withContentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    /**
     * Sets the content encoding.
     *
     * @param contentEncoding the content encoding
     * @return the builder
     */
    public Builder withContentEncoding(String contentEncoding) {
      this.contentEncoding = contentEncoding;
      return this;
    }

    /**
     * Sets the content.
     *
     * @param inputStream the content
     * @return the builder
     */
    public Builder withInputStream(InputStream inputStream) {
      this.inputStream = inputStream;
      return this;
    }

    /**
     * Sets the content.
     *
     * @param byteArray the content
     * @return the builder
     */
    public Builder withByteArray(byte[] byteArray) {
      this.contentLength = (long) byteArray.length;
      this.byteArray = byteArray;
      return this;
    }

    /**
     * Sets the content.
     *
     * @param byteArray the content
     */
    public void setByteArray(byte[] byteArray) {
      this.contentLength = (long) byteArray.length;
      this.byteArray = byteArray;
    }

    /**
     * Sets the content length.
     *
     * @param contentLength the content length
     */
    public void setContentLength(Long contentLength) {
      this.contentLength = contentLength;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the content type
     */
    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    /**
     * Sets the content encoding.
     *
     * @param contentEncoding the content encoding
     */
    public void setContentEncoding(String contentEncoding) {
      this.contentEncoding = contentEncoding;
    }

    /**
     * Sets the content.
     *
     * @param inputStream the content
     */
    public void setInputStream(InputStream inputStream) {
      this.inputStream = inputStream;
    }

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
     * Builds an immutable blob.
     *
     * @return the blob
     */
    public Blob build() {
      Blob blob = new Blob();
      blob.contentLength = contentLength;
      blob.contentEncoding = contentEncoding;
      blob.contentType = contentType;
      if (inputStream != null) {
        blob.inputStream = inputStream;
      }
      if (byteArray != null) {
        blob.inputStream = new ByteArrayInputStream(byteArray);
      }
      return blob;
    }
  }
}
