package io.gazetteer.tiles.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.io.ByteStreams;
import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.TileReader;
import io.gazetteer.tiles.TileWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3TileStore implements TileReader, TileWriter {

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
