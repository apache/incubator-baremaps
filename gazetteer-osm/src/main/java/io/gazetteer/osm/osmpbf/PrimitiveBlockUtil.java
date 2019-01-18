package io.gazetteer.osm.osmpbf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

public class PrimitiveBlocks {

    public static Stream<PrimitiveBlockReader> stream(File file) throws FileNotFoundException {
        return FileBlocks.stream(file)
                .filter(FileBlocks::isDataBlock)
                .map(FileBlocks::toDataBlock)
                .map(PrimitiveBlockReader::new);
    }

}
