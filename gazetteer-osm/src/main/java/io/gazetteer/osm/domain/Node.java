package io.gazetteer.osm.domain;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Node implements Entity {

    private final Info info;

    private final double lon;

    private final double lat;

    public Node(Info info, double lon, double lat) {
        checkNotNull(info);
        this.info = info;
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public Info getInfo() {
        return info;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Double.compare(node.lon, lon) == 0 &&
                Double.compare(node.lat, lat) == 0 &&
                Objects.equal(info, node.info);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(info, lon, lat);
    }
}



