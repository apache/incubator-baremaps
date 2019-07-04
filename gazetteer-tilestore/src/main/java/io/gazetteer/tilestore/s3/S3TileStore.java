package io.gazetteer.tilestore.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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

  private final String bucket;

  private final AmazonS3 s3;

  public S3TileStore(String bucket) {
    this.s3 = AmazonS3ClientBuilder.standard().defaultClient();
    this.bucket = bucket;
  }

  @Override
  public Tile read(XYZ xyz) throws TileException {
    try {
      byte[] bytes = ByteStreams.toByteArray(s3.getObject(bucket, path(xyz)).getObjectContent());
      return new Tile(bytes);
    } catch (IOException e) {
      throw new TileException(e);
    }
  }

  @Override
  public void write(XYZ xyz, Tile tile) throws TileException {
    s3.putObject(bucket, path(xyz), new ByteArrayInputStream(tile.getBytes()), new ObjectMetadata());
  }

  private String path(XYZ xyz) {
    return "/" + xyz.getZ() +
        "/" + xyz.getX() +
        "/" + xyz.getY() + ".pbf";
  }

}
