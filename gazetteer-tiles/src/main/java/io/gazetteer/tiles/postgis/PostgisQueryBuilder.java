package io.gazetteer.tiles.postgis;

import com.google.common.base.Joiner;
import io.gazetteer.common.postgis.GeometryUtils;
import io.gazetteer.tiles.Tile;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PostgisQueryBuilder {

  private static final Joiner JOIN_VALUES = Joiner.on(" || ").skipNulls();
  private static final Joiner JOIN_SOURCES = Joiner.on(", ").skipNulls();

  // {0} = values; {1} = sources
  private static final String SQL_LAYERS = "SELECT {0} FROM {1}";

  // {0} = name;
  private static final String SQL_VALUE = "ST_AsMVT({0}, ''{0}'', 4096, ''geom'')";

  // {0} = name; {1} = sql; {2} = envelope
  private static final String SQL_SOURCE =
      "(SELECT id, "
          + "(tags || hstore(''geometry'', lower(replace(st_geometrytype(geom), ''ST_'', ''''))))::jsonb, "
          + "ST_AsMvtGeom(geom, {2}, 4096, 256, true) AS geom "
          + "FROM ({1}) AS layer "
          + "WHERE geom && {2} AND ST_Intersects(geom, {2})"
          + ") as {0}";

  // {0} = minX; {1} = minY; {2} = maxX; {3} = maxY
  private static final String SQL_ENVELOPE = "ST_MakeEnvelope({0}, {1}, {2}, {3}, 3857)";

  private static final double EARTH_CIRCUMFERENCE = 40075016.686;

  public static String build(Tile tile, PostgisLayer layer) {
    String value = buildValue(layer);
    String source = buildSource(tile, layer);
    return MessageFormat.format(SQL_LAYERS, value, source);
  }

  public static String build(Tile tile, List<PostgisLayer> layers) {
    List<String> values = buildValues(layers);
    List<String> sources = buildSources(tile, layers);
    return MessageFormat.format(SQL_LAYERS, JOIN_VALUES.join(values), JOIN_SOURCES.join(sources));
  }

  protected static String interpolateVariables(Tile tile, String sql) {
    // tile getOverlappingXYZ
    sql = sql.replace("{x}", Integer.toString(tile.getX()));
    sql = sql.replace("{y}", Integer.toString(tile.getY()));
    sql = sql.replace("{z}", Integer.toString(tile.getZ()));

    // pixel area
    double pixelArea =
        Math.pow(
            EARTH_CIRCUMFERENCE
                * Math.cos(Tile.tile2lat(tile.getY(), tile.getZ()))
                / Math.pow(2, tile.getZ())
                / 256,
            2);
    sql = sql.replace("{pixelArea}", Double.toString(pixelArea));

    return sql;
  }

  protected static List<String> buildValues(List<PostgisLayer> layers) {
    List<String> values = new ArrayList<>();
    for (PostgisLayer layer : layers) {
      values.add(buildValue(layer));
    }
    return values;
  }

  protected static String buildValue(PostgisLayer layer) {
    return MessageFormat.format(SQL_VALUE, layer.getName());
  }

  protected static List<String> buildSources(Tile tile, List<PostgisLayer> layers) {
    List<String> sources = new ArrayList<>();
    for (PostgisLayer layer : layers) {
      sources.add(buildSource(tile, layer));
    }
    return sources;
  }

  protected static String buildSource(Tile tile, PostgisLayer layer) {
    Envelope envelope = tile.envelope();
    Coordinate min = GeometryUtils.toCoordinate(envelope.getMinX(), envelope.getMinY());
    Coordinate max = GeometryUtils.toCoordinate(envelope.getMaxX(), envelope.getMaxY());
    String value =
        MessageFormat.format(
            SQL_ENVELOPE,
            Double.toString(min.getX()),
            Double.toString(min.getY()),
            Double.toString(max.getX()),
            Double.toString(max.getY()));
    String sql = MessageFormat.format(SQL_SOURCE, layer.getName(), layer.getSql(), value);
    return interpolateVariables(tile, sql);
  }
}
