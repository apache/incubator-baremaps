package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.domain.Entity;
import io.gazetteer.osm.osmpbf.PbfFileReader;
import io.gazetteer.osm.util.BatchSpliterator;

import javax.xml.stream.XMLEventReader;
import java.util.Spliterator;
import java.util.function.Consumer;

public class FileSpliterator  extends BatchSpliterator<Entity> {

    private final XMLEventReader reader;

    public FileSpliterator(XMLEventReader reader, int batchSize) {
        super(batchSize, ORDERED | DISTINCT | NONNULL | IMMUTABLE);
        this.reader = reader;
    }

    public FileSpliterator(PbfFileReader reader) {
        this(reader, 10);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Entity> consumer) {
        return false;
    }

    @Override
    public Spliterator<Entity> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return 0;
    }

}
