package io.gazetteer.osm;

import com.google.protobuf.ByteString;
import io.gazetteer.osm.osmpbf.FileBlock;

import java.io.File;

public class OSMTestUtil {

    public static final File OSM_PBF_DATA = new File(OSMTestUtil.class.getClassLoader().getResource("data.osm.pbf").getFile());

    public static final File OSM_PBF_DENSE_BLOCK = new File(OSMTestUtil.class.getClassLoader().getResource("dense.osm.pbf").getFile());

    public static final File OSM_PBF_WAYS_BLOCK = new File(OSMTestUtil.class.getClassLoader().getResource("ways.osm.pbf").getFile());

    public static final File OSM_PBF_RELATIONS_BLOCK = new File(OSMTestUtil.class.getClassLoader().getResource("relations.osm.pbf").getFile());

    public static final FileBlock OSM_PBF_INVALID_BLOCK = new FileBlock("", ByteString.copyFromUtf8(""), ByteString.copyFromUtf8(""));

    public static final File OSM_XML_DATA = new File(OSMTestUtil.class.getClassLoader().getResource("data.osm.xml").getFile());

    public static final File OSC_XML_DATA = new File(OSMTestUtil.class.getClassLoader().getResource("data.osc.xml").getFile());

}
