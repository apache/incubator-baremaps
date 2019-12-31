package io.gazetteer.tiles.postgis;

import com.google.common.base.Joiner;
import io.gazetteer.tiles.Tile;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class PostgisQueryBuilder {

  private final CRSFactory crsFactory = new CRSFactory();
  private final CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
  private final CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
  private final CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
  private final CoordinateTransform coordinateTransform = coordinateTransformFactory
      .createTransform(epsg4326, epsg3857);

  private static final Joiner JOIN_VALUES = Joiner.on(" || ").skipNulls();
  private static final Joiner JOIN_SOURCES = Joiner.on(", ").skipNulls();

  // {0} = values; {1} = sources
  private static final String SQL_LAYERS = "SELECT {0} FROM {1}";

  // {0} = name;
  private static final String SQL_VALUE = "ST_AsMVT(mvt_geom, ''{0}'', 4096, ''geom'')";

  // {0} = name; {1} = sql; {2} = envelope
  private static final String SQL_SOURCE =
      "(SELECT id, "
          + "(tags || hstore(''geometry'', lower(replace(st_geometrytype(geom), ''ST_'', ''''))))::jsonb, "
          + "ST_AsMvtGeom(geom, {2}, 4096, 256, true) AS geom "
          + "FROM ({1}) AS layer "
          + "WHERE geom && {2} AND ST_Intersects(geom, {2})"
          + ") as mvt_geom";

  // {0} = minX; {1} = minY; {2} = maxX; {3} = maxY
  private static final String SQL_ENVELOPE = "ST_MakeEnvelope({0}, {1}, {2}, {3}, 3857)";

  private static final double EARTH_CIRCUMFERENCE = 40075016.686;

  public String build(Tile tile, PostgisLayer layer) {
    String value = buildValue(layer);
    String source = buildSource(tile, layer);
    return MessageFormat.format(SQL_LAYERS, value, source);
  }

  public String build(Tile tile, List<PostgisLayer> layers) {
    List<String> values = buildValues(layers);
    List<String> sources = buildSources(tile, layers);
    return MessageFormat.format(SQL_LAYERS, JOIN_VALUES.join(values), JOIN_SOURCES.join(sources));
  }

  protected String interpolateVariables(Tile tile, String sql) {
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

  protected List<String> buildValues(List<PostgisLayer> layers) {
    List<String> values = new ArrayList<>();
    for (PostgisLayer layer : layers) {
      values.add(buildValue(layer));
    }
    return values;
  }

  protected String buildValue(PostgisLayer layer) {
    return MessageFormat.format(SQL_VALUE, layer.getName());
  }

  protected List<String> buildSources(Tile tile, List<PostgisLayer> layers) {
    List<String> sources = new ArrayList<>();
    for (PostgisLayer layer : layers) {
      sources.add(buildSource(tile, layer));
    }
    return sources;
  }

  protected Coordinate toCoordinate(double x, double y) {
    ProjCoordinate coordinate = coordinateTransform.transform(new ProjCoordinate(x, y), new ProjCoordinate());
    return new Coordinate(coordinate.x, coordinate.y);
  }

  protected String buildSource(Tile tile, PostgisLayer layer) {
    Envelope envelope = tile.envelope();
    Coordinate min = toCoordinate(envelope.getMinX(), envelope.getMinY());
    Coordinate max = toCoordinate(envelope.getMaxX(), envelope.getMaxY());
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
