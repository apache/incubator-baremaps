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

package com.baremaps.tiles.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.io.ByteStreams;
import com.baremaps.core.tile.Tile;
import com.baremaps.tiles.TileException;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.TileWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class S3TileStore implements TileReader, TileWriter {

  private static final String CONTENT_ENCODING = "gzip";

  private static final String CONTENT_TYPE = "application/vnd.mapbox-vector-tile";

  private final AmazonS3 client;

  private final AmazonS3URI uri;

  public S3TileStore(AmazonS3 client, AmazonS3URI uri) {
    this.client = client;
    this.uri = uri;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try {
      String path = getPath(tile);
      return ByteStreams.toByteArray(client.getObject(uri.getBucket(), path).getObjectContent());
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  @Override
  public void write(Tile tile, byte[] bytes) {
    String path = getPath(tile);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentEncoding(CONTENT_ENCODING);
    metadata.setContentType(CONTENT_TYPE);
    metadata.setContentLength(bytes.length);
    client.putObject(uri.getBucket(), path, new ByteArrayInputStream(bytes), metadata);
  }

  private String getPath(Tile tile) {
    if (uri.getKey() == null) {
      return String.format("%s/%s/%s.pbf", tile.getZ(), tile.getX(), tile.getY());
    } else {
      return String.format("%s/%s/%s/%s.pbf", uri.getKey(), tile.getZ(), tile.getX(), tile.getY());
    }
  }

}
