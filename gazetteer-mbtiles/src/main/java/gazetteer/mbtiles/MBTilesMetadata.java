package gazetteer.mbtiles;

import java.util.Map;

public class MBTilesMetadata {

    public final String name;

    public final MBTilesFormat format;

    public final MBTilesBounds bounds;

    public final MBTilesCenter center;

    public final int minZoom;

    public final int maxZoom;

    public final String attribution;

    public final String description;

    public final MBTilesType type;

    public final String version;

    public final String json;

    public MBTilesMetadata(String name, MBTilesFormat format, MBTilesBounds bounds, MBTilesCenter center, int minZoom, int maxZoom, String attribution, String description, MBTilesType type, String version, String json) {
        this.name = name;
        this.format = format;
        this.bounds = bounds;
        this.center = center;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.attribution = attribution;
        this.description = description;
        this.type = type;
        this.version = version;
        this.json = json;
    }

    public static MBTilesMetadata fromMap(Map<String, String> metadata) {
        String name = metadata.get("name");
        MBTilesFormat format = MBTilesFormat.valueOf(metadata.get("format"));
        MBTilesBounds bounds = MBTilesBounds.fromString(metadata.get("bounds"));
        MBTilesCenter center = MBTilesCenter.fromString(metadata.get("center"));
        int minZoom = Integer.parseInt(metadata.get("minzoom"));
        int maxZoom = Integer.parseInt(metadata.get("maxzoom"));
        String attribution = metadata.get("attribution");
        String description = metadata.get("description");
        MBTilesType type = MBTilesType.valueOf(metadata.getOrDefault("type", "baselayer"));
        String version = metadata.get("version");
        String json = metadata.get("json");
        return new MBTilesMetadata(name, format, bounds, center, minZoom, maxZoom, attribution, description, type, version, json);
    }
}
