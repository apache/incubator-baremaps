package io.gazetteer.tilestore.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.io.ByteStreams;
import io.gazetteer.tilestore.model.Tile;
import io.gazetteer.tilestore.model.TileException;
import io.gazetteer.tilestore.model.TileReader;
import io.gazetteer.tilestore.model.TileWriter;
import io.gazetteer.tilestore.model.XYZ;
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
  public Tile read(XYZ xyz) throws TileException {
    try {
      byte[] bytes = ByteStreams.toByteArray(client.getObject(bucket, path(xyz)).getObjectContent());
      return new Tile(bytes);
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  @Override
  public void write(XYZ xyz, Tile tile) {
    byte[] bytes = tile.getBytes();
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(bytes.length);
    client.putObject(bucket, path(xyz), new ByteArrayInputStream(bytes), metadata);
  }

  private String path(XYZ xyz) {
    return xyz.getZ() +
        "/" + xyz.getX() +
        "/" + xyz.getY() + ".pbf";
  }

}
