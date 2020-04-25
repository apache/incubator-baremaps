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

package com.baremaps.tiles.file;

import com.baremaps.util.tile.Tile;
import com.baremaps.tiles.TileException;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.TileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileTileStore implements TileReader, TileWriter {

  private final Path directory;

  public FileTileStore(Path directory) {
    this.directory = directory;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    Path path = path(tile);
    if (!Files.exists(path)) {
      return null;
    }
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  @Override
  public void write(Tile tile, byte[] bytes) throws TileException {
    Path file = path(tile);
    Path directory = file.getParent();
    try {
      if (!Files.exists(directory)) {
        Files.createDirectories(directory);
      }
      Files.write(file, bytes, StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  private Path path(Tile tile) {
    return directory
        .resolve(Integer.toString(tile.getZ()))
        .resolve(Integer.toString(tile.getX()))
        .resolve(tile.getY() + ".pbf");
  }

}
