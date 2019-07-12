package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.Accumulator;
import io.gazetteer.osm.util.StreamException;
import org.junit.jupiter.api.Test;

import java.util.Spliterator;
import java.util.stream.Collectors;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static io.gazetteer.osm.OSMTestUtil.osmPbfInvalidBlock;
import static org.junit.jupiter.api.Assertions.*;

public class PBFUtilTest {

    @Test
    public void stream() {
        assertTrue(PBFUtil.stream(osmPbfData()).count() == 10);
    }

    @Test
    public void isHeaderBlock() {
        assertTrue(PBFUtil.stream(osmPbfData()).filter(PBFUtil::isHeaderBlock).count() == 1);
    }

    @Test
    public void isDataBlock() {
        assertTrue(PBFUtil.stream(osmPbfData()).filter(PBFUtil::isDataBlock).count() == 9);
    }

    @Test
    public void toHeaderBlock() {
        assertTrue(
                PBFUtil.stream(osmPbfData())
                        .filter(PBFUtil::isHeaderBlock)
                        .map(PBFUtil::toHeaderBlock)
                        .count()
                        == 1);
    }

    @Test
    public void toDataBlock() {
        assertTrue(
                PBFUtil.stream(osmPbfData())
                        .filter(PBFUtil::isDataBlock)
                        .map(PBFUtil::toPrimitiveBlock)
                        .collect(Collectors.toList())
                        .size()
                        == 9);
    }

    @Test
    public void toDataBlockException() {
        assertThrows(StreamException.class, () -> {
            PBFUtil.toPrimitiveBlock(osmPbfInvalidBlock());
        });
    }

    @Test
    public void tryAdvance() {
        Spliterator<FileBlock> spliterator = PBFUtil.spliterator(osmPbfData());
        for (int i = 0; i < 10; i++) {
            assertTrue(spliterator.tryAdvance(block -> {
            }));
        }
        assertFalse(spliterator.tryAdvance(block -> {
        }));
    }

    @Test
    public void forEachRemaining() {
        Spliterator<FileBlock> spliterator = PBFUtil.spliterator(osmPbfData());
        Accumulator<FileBlock> accumulator = new Accumulator<>();
        spliterator.forEachRemaining(accumulator);
        assertTrue(accumulator.acc.size() == 10);
    }
}
