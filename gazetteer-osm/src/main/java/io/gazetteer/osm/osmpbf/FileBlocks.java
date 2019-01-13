package io.gazetteer.osm.osmpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FileBlocks {

    public static final String HEADER = "OSMHeader";
    public static final String DATA = "OSMData";

    public static Stream<FileBlock> stream(File file) throws FileNotFoundException {
        DataInputStream input = new DataInputStream(new FileInputStream(file));
        FileBlockReader reader = new FileBlockReader(input);
        return StreamSupport.stream(new FileBlockSpliterator(reader), true);
    }

    public static boolean isHeaderBlock(FileBlock fileBlock) {
        return fileBlock.type.equals(HEADER);
    }

    public static boolean isDataBlock(FileBlock fileBlock) {
        return fileBlock.type.equals(DATA);
    }

    public static Osmformat.HeaderBlock toHeaderBlock(FileBlock fileBlock) {
        try {
            return Osmformat.HeaderBlock.parseFrom(fileBlock.data);
        } catch (InvalidProtocolBufferException e) {
            throw new Error("Unable to stream header block");
        }
    }

    public static Osmformat.PrimitiveBlock toDataBlock(FileBlock fileBlock) {
        try {
            return Osmformat.PrimitiveBlock.parseFrom(fileBlock.data);
        } catch (InvalidProtocolBufferException e) {
            throw new Error("Unable to stream primitive block");
        }
    }


}
