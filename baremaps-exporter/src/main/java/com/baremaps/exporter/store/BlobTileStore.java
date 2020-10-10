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

package com.baremaps.exporter.store;

import com.baremaps.util.tile.Tile;
import com.baremaps.util.storage.BlobStore;
import java.io.IOException;
import java.net.URI;

public class BlobTileStore implements TileStore {

  private final BlobStore blobStore;

  private final URI uri;

  public BlobTileStore(BlobStore blobStore, URI uri) {
    this.blobStore = blobStore;
    this.uri = uri;
  }

  @Override
  public byte[] read(Tile tile) throws TileStoreException {
    try {
      return blobStore.readByteArray(getURI(tile));
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }

  @Override
  public void write(Tile tile, byte[] bytes) throws TileStoreException {
    try {
      blobStore.writeByteArray(getURI(tile), bytes);
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }

  @Override
  public void delete(Tile tile) throws TileStoreException {
    try {
      blobStore.delete(getURI(tile));
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }

  public URI getURI(Tile tile) {
    return uri.resolve(String.format("%s/%s/%s.pbf", tile.z(), tile.x(), tile.y()));
  }

}
