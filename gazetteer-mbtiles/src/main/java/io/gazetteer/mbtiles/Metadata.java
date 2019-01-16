package io.gazetteer.mbtiles;

import java.util.Map;

public class Metadata {

    public final String name;

    public final Format format;

    public final Bounds bounds;

    public final Center center;

    public final int minZoom;

    public final int maxZoom;

    public final String attribution;

    public final String description;

    public final Type type;

    public final String version;

    public final String json;

    public Metadata(String name, Format format, Bounds bounds, Center center, int minZoom, int maxZoom, String attribution, String description, Type type, String version, String json) {
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

    public static Metadata fromMap(Map<String, String> metadata) {
        String name = metadata.get("name");
        Format format = Format.valueOf(metadata.get("format"));
        Bounds bounds = Bounds.deserialize(metadata.get("bounds"));
        Center center = Center.deserialize(metadata.get("center"));
        int minZoom = Integer.parseInt(metadata.get("minzoom"));
        int maxZoom = Integer.parseInt(metadata.get("maxzoom"));
        String attribution = metadata.get("attribution");
        String description = metadata.get("description");
        Type type = Type.valueOf(metadata.getOrDefault("type", "baselayer"));
        String version = metadata.get("version");
        String json = metadata.get("json");
        return new Metadata(name, format, bounds, center, minZoom, maxZoom, attribution, description, type, version, json);
    }
}
