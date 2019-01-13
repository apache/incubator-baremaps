package gazetteer.osm.model;

public class Node implements Entity {

    private final Data data;

    private final double lon;

    private final double lat;

    public Node(Data data, double lon, double lat) {
        this.data = data;
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public Data getData() {
        return data;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

}



