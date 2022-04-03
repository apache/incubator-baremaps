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

package com.baremaps.core.tile;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreException;
import java.net.URI;

/** Represents a {@code TileStore} baked by a {@code BlobStore}. */
public class TileBlobStore implements TileStore {

  private final BlobStore blobStore;

  private final URI uri;

  /**
   * Constructs a {@code TileBlobStore}.
   *
   * @param blobStore the underlying blobstore
   * @param baseURI the base URI
   */
  public TileBlobStore(BlobStore blobStore, URI baseURI) {
    this.blobStore = blobStore;
    this.uri = baseURI;
  }

  /** {@inheritDoc} */
  @Override
  public Blob read(Tile tile) throws TileStoreException {
    try {
      return blobStore.get(getURI(tile));
    } catch (BlobStoreException e) {
      throw new TileStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void write(Tile tile, Blob blob) throws TileStoreException {
    try {
      blobStore.put(getURI(tile), blob);
    } catch (BlobStoreException e) {
      throw new TileStoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(Tile tile) throws TileStoreException {
    try {
      blobStore.delete(getURI(tile));
    } catch (BlobStoreException e) {
      throw new TileStoreException(e);
    }
  }

  public URI getURI(Tile tile) {
    return uri.resolve(String.format("%s/%s/%s.mvt", tile.z(), tile.x(), tile.y()));
  }
}
