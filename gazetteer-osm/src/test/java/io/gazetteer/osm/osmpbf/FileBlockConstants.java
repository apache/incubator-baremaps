package io.gazetteer.osm.osmpbf;

import com.google.protobuf.ByteString;

import java.io.File;

public class FileBlockConstants {

    public static final File BLOCKS = new File(FileBlockConstants.class.getClassLoader().getResource("blocks.osm").getFile());

    public static final File DENSE_BLOCK = new File(FileBlockConstants.class.getClassLoader().getResource("dense.osm").getFile());

    public static final File WAYS_BLOCK = new File(FileBlockConstants.class.getClassLoader().getResource("ways.osm").getFile());

    public static final File RELATIONS_BLOCK = new File(FileBlockConstants.class.getClassLoader().getResource("relations.osm").getFile());

    public static final FileBlock INVALID_BLOCK = new FileBlock("", ByteString.copyFromUtf8(""), ByteString.copyFromUtf8(""));

}
