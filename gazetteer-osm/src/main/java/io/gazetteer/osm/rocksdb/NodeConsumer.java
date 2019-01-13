package io.gazetteer.osm.rocksdb;

import io.gazetteer.osm.domain.Node;

import java.util.Collection;
import java.util.function.Consumer;

public class NodeConsumer implements Consumer<Collection<Node>> {

    private final EntityStore<Node> entityStore;

    public NodeConsumer(EntityStore<Node> entityStore) {
        this.entityStore = entityStore;
    }

    @Override
    public void accept(Collection<Node> nodes) {
        try {
            entityStore.addAll(nodes);
        } catch (EntityStoreException e) {
            e.printStackTrace();
        }
    }

}
