package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;
import io.gazetteer.osm.util.BatchSpliterator;
import io.gazetteer.osm.util.WrappedException;

import java.io.EOFException;
import java.util.function.Consumer;

public class EntitySpliterator extends BatchSpliterator<Entity> {

    private final EntityReader reader;

    public EntitySpliterator(EntityReader reader) {
        this(reader, 1000);
    }

    public EntitySpliterator(EntityReader reader, int batchSize) {
        super(batchSize, ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        this.reader = reader;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Entity> consumer) {
        try {
            Entity entity = reader.read();
            consumer.accept(entity);
            return true;
        } catch (EOFException e) {
            return false;
        } catch (Exception e) {
            throw new WrappedException(e);
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super Entity> consumer) {
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
