/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.tilestore.pmtiles;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.baremaps.maplibre.tileset.Tileset;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;

public class PMTilesStore implements TileStore {

  private final PMTilesWriter writer;

  public PMTilesStore(Path path, Tileset tileset) {
    try {
      var metadata = new HashMap<String, Object>();
      metadata.put("name", tileset.getName());
      metadata.put("type", "baselayer");
      metadata.put("version", tileset.getVersion());
      metadata.put("description", tileset.getDescription());
      metadata.put("attribution", tileset.getAttribution());
      metadata.put("vector_layers", tileset.getVectorLayers());

      var minZoom = Optional.ofNullable(tileset.getMinzoom()).orElse(0);
      var maxZoom = Optional.ofNullable(tileset.getMaxzoom()).orElse(14);
      var bounds = Optional.ofNullable(tileset.getBounds()).orElse(List.of(-180d, -90d, 180d, 90d));
      var center = Optional.ofNullable(tileset.getCenter()).orElse(List.of(0d, 0d, 3d));

      writer = new PMTilesWriter(path);
      writer.setMetadata(metadata);
      writer.setMinZoom(minZoom);
      writer.setMaxZoom(maxZoom);
      writer.setMinLon(bounds.get(0));
      writer.setMinLat(bounds.get(1));
      writer.setMaxLon(bounds.get(2));
      writer.setMaxLat(bounds.get(3));
      writer.setCenterLon(center.get(0));
      writer.setCenterLat(center.get(1));
      writer.setMinZoom(tileset.getMinzoom());
      writer.setMaxZoom(tileset.getMaxzoom());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException {
    try {
      writer.setTile(tileCoord.z(), tileCoord.x(), tileCoord.y(), blob.array());
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }

  @Override
  public void delete(TileCoord tileCoord) throws TileStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws TileStoreException {
    try {
      writer.write();
    } catch (IOException e) {
      throw new TileStoreException(e);
    }
  }
}
