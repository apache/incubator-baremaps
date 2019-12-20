package io.gazetteer.tiles.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.io.ByteStreams;
import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.TileReader;
import io.gazetteer.tiles.TileWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class S3TileStore implements TileReader, TileWriter {

  private final AmazonS3 client;

  private final String bucket;

  public S3TileStore(AmazonS3 client, String bucket) {
    this.client = client;
    this.bucket = bucket;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try {
      return ByteStreams.toByteArray(client.getObject(bucket, path(tile)).getObjectContent());
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  @Override
  public void write(Tile tile, byte[] bytes) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(bytes.length);
    client.putObject(bucket, path(tile), new ByteArrayInputStream(bytes), metadata);
  }

  private String path(Tile tile) {
    return tile.getZ() +
        "/" + tile.getX() +
        "/" + tile.getY() + ".pbf";
  }

}
