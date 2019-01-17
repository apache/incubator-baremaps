package io.gazetteer.osm.osmpbf;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.gazetteer.osm.osmpbf.FileBlockConstants.TEN_BLOCKS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileBlockSpliteratorTest {

    public static class Accumulator implements Consumer<FileBlock> {

        public List<FileBlock> fileBlocks = new ArrayList<>();

        @Override
        public void accept(FileBlock fileBlock) {
            fileBlocks.add(fileBlock);
        }
    }

    @Test
    public void tryAdvance() throws FileNotFoundException {
        FileBlockSpliterator spliterator = FileBlocks.spliterator(TEN_BLOCKS);
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {}));
        }
        assertFalse(spliterator.tryAdvance(block -> {}));
    }

    @Test
    public void forEachRemaining() throws FileNotFoundException {
        FileBlockSpliterator spliterator = FileBlocks.spliterator(TEN_BLOCKS);;
        Accumulator accumulator = new Accumulator();
        spliterator.forEachRemaining(accumulator);
        assertTrue(accumulator.fileBlocks.size() == 10);
    }

}