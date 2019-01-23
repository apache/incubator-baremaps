package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Change;
import io.gazetteer.osm.util.BatchSpliterator;
import io.gazetteer.osm.util.WrappedException;

import java.io.EOFException;
import java.util.function.Consumer;

public class ChangeSpliterator extends BatchSpliterator<Change> {

    private final ChangeReader reader;

    public ChangeSpliterator(ChangeReader reader) {
        this(reader, 1000);
    }

    public ChangeSpliterator(ChangeReader reader, int batchSize) {
        super(batchSize, ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        this.reader = reader;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Change> consumer) {
        try {
            Change entity = reader.read();
            consumer.accept(entity);
            return true;
        } catch (EOFException e) {
            return false;
        } catch (Exception e) {
            throw new WrappedException(e);
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super Change> consumer) {
        try {
            while (true) {
                consumer.accept(reader.read());
            }
        } catch (EOFException e) {
            // reached the end of the file
        } catch (Exception e) {
            throw new WrappedException(e);
        }
    }
}
