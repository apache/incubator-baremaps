package io.gazetteer.osm.osmpbf;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.core.stream.StreamException;
import io.gazetteer.osm.binary.Osmformat;

public final class FileBlock {

  private final Type type;
  private final ByteString indexdata;
  private final ByteString data;

  public FileBlock(Type type, ByteString indexdata, ByteString blob) {
    checkNotNull(type);
    checkNotNull(indexdata);
    checkNotNull(blob);
    this.type = type;
    this.indexdata = indexdata;
    this.data = blob;
  }

  public Type getType() {
    return type;
  }

  public boolean isHeaderBlock() {
    return getType() == Type.OSMHeader;
  }

  public boolean isPrimitiveBlock() {
    return getType() == Type.OSMData;
  }

  public HeaderBlock toHeaderBlock() {
    try {
      return new HeaderBlock(Osmformat.HeaderBlock.parseFrom(getData()));
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public PrimitiveBlock toPrimitiveBlock() {
    try {
      return new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(getData()));
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public ByteString getIndexdata() {
    return indexdata;
  }

  public ByteString getData() {
    return data;
  }

  public enum Type {
    OSMHeader,
    OSMData
  }
}
