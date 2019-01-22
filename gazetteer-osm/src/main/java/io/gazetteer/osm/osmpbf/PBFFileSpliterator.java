package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.BatchSpliterator;
import io.gazetteer.osm.util.WrappedException;

import java.io.EOFException;
import java.io.IOException;
import java.util.function.Consumer;

public class PBFFileSpliterator extends BatchSpliterator<FileBlock> {

    private final PBFFileReader reader;

    public PBFFileSpliterator(PBFFileReader reader, int batchSize) {
        super(batchSize, ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        this.reader = reader;
    }

    public PBFFileSpliterator(PBFFileReader reader) {
        this(reader, 10);
    }


    @Override
    public boolean tryAdvance(Consumer<? super FileBlock> action) {
        try {
            final FileBlock block = reader.read();
            action.accept(block);
            return true;
        } catch (EOFException e) {
            return false;
        } catch (IOException e) {
            throw new WrappedException(e);
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super FileBlock> action) {
        try {
            for (FileBlock block; (block = reader.read()) != null; ) {
                action.accept(block);
            }
        } catch (EOFException e) {
            // reached the end of the file
        } catch (Exception e) {
            throw new WrappedException(e);
        }
    }

}

