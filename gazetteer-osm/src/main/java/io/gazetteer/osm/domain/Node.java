package io.gazetteer.osm.domain;

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

}



