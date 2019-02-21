package io.gazetteer.osm;

import com.google.protobuf.ByteString;
import io.gazetteer.osm.osmpbf.FileBlock;

import java.io.InputStream;

public class OSMTestUtil {

  public static InputStream osmPbfData() {
    return OSMTestUtil.class.getClassLoader().getResourceAsStream("data.osm.pbf");
  }

  public static InputStream osmPbfDenseBlock() {
    return OSMTestUtil.class.getClassLoader().getResourceAsStream("dense.osm.pbf");
  }

  public static InputStream osmPbfWaysBlock() {
    return OSMTestUtil.class.getClassLoader().getResourceAsStream("ways.osm.pbf");
  }

  public static InputStream osmPbfRelationsBlock() {
    return OSMTestUtil.class.getClassLoader().getResourceAsStream("relations.osm.pbf");
  }

  public static FileBlock osmPbfInvalidBlock() {
    return new FileBlock("", ByteString.copyFromUtf8(""), ByteString.copyFromUtf8(""));
  }

  public static InputStream osmXmlData() {
    return OSMTestUtil.class.getClassLoader().getResourceAsStream("data.osm.xml");
  }

  public static InputStream oscXmlData() {
    return OSMTestUtil.class.getClassLoader().getResourceAsStream("data.osc.xml");
  }
}
