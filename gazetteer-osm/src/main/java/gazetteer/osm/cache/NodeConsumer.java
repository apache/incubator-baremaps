package gazetteer.osm.cache;

import gazetteer.osm.model.Node;

import java.util.Collection;
import java.util.function.Consumer;

public class NodeConsumer implements Consumer<Collection<Node>> {

    private final EntityCache<Node> entityCache;

    public NodeConsumer(EntityCache<Node> entityCache) {
        this.entityCache = entityCache;
    }

    @Override
    public void accept(Collection<Node> nodes) {
        try {
            entityCache.addAll(nodes);
        } catch (EntityCacheException e) {
            e.printStackTrace();
        }
    }

}
