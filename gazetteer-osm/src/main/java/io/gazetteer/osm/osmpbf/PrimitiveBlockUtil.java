package io.gazetteer.osm.osmpbf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

public class PrimitiveBlockUtil {

    public static Stream<PrimitiveBlockReader> stream(File file) throws FileNotFoundException {
        return PBFFileUtil.stream(file)
                .filter(PBFFileUtil::isDataBlock)
                .map(PBFFileUtil::toDataBlock)
                .map(PrimitiveBlockReader::new);
    }

}
