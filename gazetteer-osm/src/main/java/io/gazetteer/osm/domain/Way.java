package io.gazetteer.osm.domain;

import java.util.List;

public class Way implements Entity {

    private final Info info;

    private final List<Long> nodes;

    public Way(Info info, List<Long> nodes) {
        this.info = info;
        this.nodes = nodes;
    }

    @Override
    public Info getInfo() {
        return info;
    }

    public List<Long> getNodes() {
        return nodes;
    }

}
