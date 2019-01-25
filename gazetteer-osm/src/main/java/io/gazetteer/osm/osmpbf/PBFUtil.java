package io.gazetteer.osm.osmpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.util.BatchSpliterator;
import io.gazetteer.osm.util.WrappedException;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PBFUtil {

  public static final String HEADER = "OSMHeader";
  public static final String DATA = "OSMData";

  public static Iterator<FileBlock> iterator(File file) throws FileNotFoundException {
    DataInputStream input = new DataInputStream(new FileInputStream(file));
    return new FileBlockIterator(input);
  }

  public static Spliterator<FileBlock> spliterator(File file) throws FileNotFoundException {
    return new BatchSpliterator<FileBlock>(iterator(file), 10);
  }

  public static Stream<FileBlock> fileBlocks(File file) throws FileNotFoundException {
    return StreamSupport.stream(spliterator(file), true);
  }

  public static Stream<DataBlockReader> dataBlockReaders(File file) throws FileNotFoundException {
    return PBFUtil.fileBlocks(file)
        .filter(PBFUtil::isDataBlock)
        .map(PBFUtil::toDataBlock)
        .map(DataBlockReader::new);
  }

  public static boolean isHeaderBlock(FileBlock fileBlock) {
    return fileBlock.getType().equals(HEADER);
  }

  public static boolean isDataBlock(FileBlock fileBlock) {
    return fileBlock.getType().equals(DATA);
  }

  public static Osmformat.HeaderBlock toHeaderBlock(FileBlock fileBlock) {
    try {
      return Osmformat.HeaderBlock.parseFrom(fileBlock.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new WrappedException(e);
    }
  }

  public static Osmformat.PrimitiveBlock toDataBlock(FileBlock fileBlock) {
    try {
      return Osmformat.PrimitiveBlock.parseFrom(fileBlock.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new WrappedException(e);
    }
  }
}
