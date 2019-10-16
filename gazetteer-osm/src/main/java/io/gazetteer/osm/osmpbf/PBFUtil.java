package io.gazetteer.osm.osmpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.common.stream.StreamException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

public class PBFUtil {

  private static final String HEADER_BLOCK = "OSMHeader";
  private static final String PRIMITIVE_BLOCK = "OSMData";

  public static URL url(String source) throws MalformedURLException {
    if (Files.exists(Paths.get(source))) {
      return Paths.get(source).toUri().toURL();
    } else {
      return new URL(source);
    }
  }

  public static InputStream input(File file) throws IOException {
    return new BufferedInputStream(new FileInputStream(file));
  }

  public static InputStream input(URL url) throws IOException {
    return new BufferedInputStream(url.openConnection().getInputStream());
  }

  public static Spliterator<FileBlock> spliterator(InputStream input) {
    return new FileBlockSpliterator(new DataInputStream(input));
  }

  public static Stream<FileBlock> stream(InputStream input) {
    return StreamSupport.stream(spliterator(input), true);
  }

  public static Stream<PrimitiveBlock> toPrimitiveBlock(Stream<FileBlock> blocks) {
    return blocks
        .filter(PBFUtil::isPrimitiveBlock)
        .map(PBFUtil::toPrimitiveBlock)
        .map(PrimitiveBlock::new);
  }

  public static boolean isHeaderBlock(FileBlock block) {
    return block.getType().equals(HEADER_BLOCK);
  }

  public static boolean isPrimitiveBlock(FileBlock block) {
    return block.getType().equals(PRIMITIVE_BLOCK);
  }

  public static Osmformat.HeaderBlock toHeaderBlock(FileBlock fileBlock) {
    try {
      return Osmformat.HeaderBlock.parseFrom(fileBlock.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public static Osmformat.PrimitiveBlock toPrimitiveBlock(FileBlock fileBlock) {
    try {
      return Osmformat.PrimitiveBlock.parseFrom(fileBlock.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

}
