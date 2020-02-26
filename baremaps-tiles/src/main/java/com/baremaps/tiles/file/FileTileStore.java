package com.baremaps.tiles.file;

import com.baremaps.tiles.Tile;
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
