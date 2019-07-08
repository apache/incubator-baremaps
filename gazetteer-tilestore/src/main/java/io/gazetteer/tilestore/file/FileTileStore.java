package io.gazetteer.tilestore.file;

import io.gazetteer.tilestore.model.Tile;
import io.gazetteer.tilestore.model.TileException;
import io.gazetteer.tilestore.model.TileReader;
import io.gazetteer.tilestore.model.TileWriter;
import io.gazetteer.tilestore.model.XYZ;
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
  public Tile read(XYZ xyz) throws TileException {
    Path path = path(xyz);
    if (!Files.exists(path)) {
      return null;
    }
    try {
      return new Tile(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  @Override
  public void write(XYZ xyz, Tile tile) throws TileException {
    Path file = path(xyz);
    Path directory = file.getParent();
    try {
      if (!Files.exists(directory)) {
        Files.createDirectories(directory);
      }
      Files.write(file, tile.getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  private Path path(XYZ xyz) {
    return directory
        .resolve(Integer.toString(xyz.getZ()))
        .resolve(Integer.toString(xyz.getX()))
        .resolve(xyz.getY() + ".pbf");
  }

}
