package gazetteer.osm.model;

import java.util.List;

public class Way implements Entity {

    private final Data data;

    private final List<Long> nodes;

    public Way(Data data, List<Long> nodes) {
        this.data = data;
        this.nodes = nodes;
    }

    @Override
    public Data getData() {
        return data;
    }

    public List<Long> getNodes() {
        return nodes;
    }

}
