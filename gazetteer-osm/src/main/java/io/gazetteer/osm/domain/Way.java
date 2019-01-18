package io.gazetteer.osm.domain;

import com.google.common.base.Objects;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Way implements Entity {

    private final Info info;

    private final List<Long> nodes;

    public Way(Info info, List<Long> nodes) {
        checkNotNull(info);
        checkNotNull(nodes);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Way way = (Way) o;
        return Objects.equal(info, way.info) &&
                Objects.equal(nodes, way.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(info, nodes);
    }
}
