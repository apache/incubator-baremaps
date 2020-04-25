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

import com.baremaps.util.tile.Tile;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.TileWriter;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3TileStore implements TileReader, TileWriter {

  private static final String CONTENT_ENCODING = "gzip";

  private static final String CONTENT_TYPE = "application/vnd.mapbox-vector-tile";

  private final S3Client client;

  private final String bucket;

  private final String root;

  public S3TileStore(S3Client client, String bucket, String root) {
    this.client = client;
    this.bucket = bucket;
    this.root = root;
  }

  @Override
  public byte[] read(Tile tile) {
      String path = getPath(tile);
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(bucket)
          .key(path)
          .build();
      return client.getObject(request, ResponseTransformer.toBytes()).asByteArray();
  }

  @Override
  public void write(Tile tile, byte[] bytes) {
    String path = getPath(tile);
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(path)
        .contentEncoding(CONTENT_ENCODING)
        .contentType(CONTENT_TYPE)
        .contentLength(Long.valueOf(bytes.length))
        .build();
    RequestBody body = RequestBody.fromBytes(bytes);
    client.putObject(request, body);
  }

  private String getPath(Tile tile) {
    if (root == null || root.equals("")) {
      return String.format("%s/%s/%s.pbf", tile.getZ(), tile.getX(), tile.getY());
    } else {
      return String.format("%s/%s/%s/%s.pbf", root, tile.getZ(), tile.getX(), tile.getY());
    }
  }

}
