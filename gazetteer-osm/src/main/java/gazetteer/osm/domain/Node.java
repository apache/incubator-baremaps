package gazetteer.osm.domain;

public class Node implements Entity {

    private final Info info;

    private final double lon;

    private final double lat;

    public Node(Info info, double lon, double lat) {
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



