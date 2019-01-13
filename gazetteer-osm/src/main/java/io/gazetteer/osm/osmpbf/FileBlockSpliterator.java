package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.BatchSpliterator;

import java.io.EOFException;
import java.io.IOException;
import java.util.function.Consumer;

public class FileBlockSpliterator extends BatchSpliterator<FileBlock> {

    private final FileBlockReader reader;

    public FileBlockSpliterator(FileBlockReader reader, int batchSize) {
        super(batchSize, ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        this.reader = reader;
    }

    public FileBlockSpliterator(FileBlockReader reader) {
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
            e.printStackTrace();
            throw new RuntimeException("Parse Error");
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
            e.printStackTrace();
            throw new RuntimeException("Parse Error");
        }
    }
}

