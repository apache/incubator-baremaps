package gazetteer.osm.leveldb;

import gazetteer.osm.model.Node;

import java.util.Map;
import java.util.function.Consumer;

public class LdbBulkInsertConsumer implements Consumer<Map<Long, Node>> {

    private final DataStore<Node> dataStore;

    public LdbBulkInsertConsumer(DataStore<Node> dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void accept(Map<Long, Node> nodes) {
        dataStore.addAll(nodes);
    }

}
