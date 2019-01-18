package io.gazetteer.osm.osmpbf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

public class PrimitiveBlockUtil {

    public static Stream<PrimitiveBlockReader> stream(File file) throws FileNotFoundException {
        return FileBlockUtil.stream(file)
                .filter(FileBlockUtil::isDataBlock)
                .map(FileBlockUtil::toDataBlock)
                .map(PrimitiveBlockReader::new);
    }

}
