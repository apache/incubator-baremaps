package io.gazetteer.osm.osmpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.util.StreamException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;

public class PBFUtil {

  public static final String HEADER = "OSMHeader";
  public static final String DATA = "OSMData";

  public static InputStream read(File file) throws IOException {
    return new BufferedInputStream(new FileInputStream(file));
  }

  public static InputStream read(URL url) throws IOException {
    return new BufferedInputStream(url.openConnection().getInputStream());
  }

  public static Spliterator<FileBlock> spliterator(InputStream input) {
    return new FileBlockSpliterator(new DataInputStream(input));
  }

  public static Stream<FileBlock> stream(InputStream input) {
    return StreamSupport.stream(spliterator(input), true);
  }

  public static HeaderBlock header(Stream<FileBlock> blocks) {
    return blocks.findFirst().map(PBFUtil::toHeaderBlock).get();
  }

  public static Stream<DataBlock> data(Stream<FileBlock> blocks) {
    return blocks
        .filter(PBFUtil::isDataBlock)
        .map(PBFUtil::toDataBlock)
        .map(DataBlockBuilder::new)
        .map(DataBlockBuilder::build);
  }

  public static boolean isHeaderBlock(FileBlock block) {
    return block.getType().equals(HEADER);
  }

  public static boolean isDataBlock(FileBlock block) {
    return block.getType().equals(DATA);
  }

  public static HeaderBlock toHeaderBlock(FileBlock fileBlock) {
    try {
      return Osmformat.HeaderBlock.parseFrom(fileBlock.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public static Osmformat.PrimitiveBlock toDataBlock(FileBlock fileBlock) {
    try {
      return Osmformat.PrimitiveBlock.parseFrom(fileBlock.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }
}
