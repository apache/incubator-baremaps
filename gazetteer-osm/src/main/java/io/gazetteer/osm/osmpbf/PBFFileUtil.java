package io.gazetteer.osm.osmpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gazetteer.osm.util.WrappedException;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PbfFileUtil {

    public static final String HEADER = "OSMHeader";
    public static final String DATA = "OSMData";

    public static PbfFileReader reader(File file) throws FileNotFoundException {
        DataInputStream input = new DataInputStream(new FileInputStream(file));
        return new PbfFileReader(input);
    }

    public static PbfFileSpliterator spliterator(File file) throws FileNotFoundException {
        return new PbfFileSpliterator(reader(file));
    }

    public static Stream<FileBlock> stream(File file) throws FileNotFoundException {
        return StreamSupport.stream(spliterator(file), true);
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
