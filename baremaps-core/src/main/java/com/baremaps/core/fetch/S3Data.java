/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.core.fetch;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class S3Data implements Data {

  private final AmazonS3 client;

  private final URI uri;

  public S3Data(AmazonS3 client, URI uri) {
    this.client = client;
    this.uri = uri;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    AmazonS3URI s3URI = new AmazonS3URI(uri);
    S3Object s3Object = client.getObject(s3URI.getBucket(), s3URI.getKey());
    return s3Object.getObjectContent();
  }

}
