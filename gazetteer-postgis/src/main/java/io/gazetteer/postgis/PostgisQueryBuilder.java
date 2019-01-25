package io.gazetteer.postgis;

import com.google.common.base.Joiner;
import io.gazetteer.core.XYZ;
import mil.nga.sf.GeometryEnvelope;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PostgisQueryBuilder {

  private static final Joiner JOIN_VALUES = Joiner.on(" || ").skipNulls();
  private static final Joiner JOIN_SOURCES = Joiner.on(", ").skipNulls();

  // {0} = values; {1} = sources
  private static final String SQL_LAYERS = "SELECT {0} FROM {1}";

  // {0} = name;
  private static final String SQL_VALUE = "ST_AsMVT({0}, ''{0}'', 4096, ''geometry'')";

  // {0} = name; {1} = sql; {2} = envelope
  private static final String SQL_SOURCE =
      "(SELECT id, properties, ST_AsMvtGeom(geometry, {2}, 4096, 256, true) AS geometry "
          + "FROM ({1}) AS layer "
          + "WHERE geometry && {2} AND ST_Intersects(geometry, {2})) as {0}";

  // {0} = minX; {1} = minY; {2} = maxX; {3} = maxY
  private static final String SQL_ENVELOPE = "ST_MakeEnvelope({0}, {1}, {2}, {3})";

  public static String build(XYZ xyz, PostgisLayer layer) {
    String value = buildValue(layer);
    String source = buildSource(xyz, layer);
    return MessageFormat.format(SQL_LAYERS, value, source);
  }

  public static String build(XYZ xyz, List<PostgisLayer> layers) {
    List<String> values = buildValues(layers);
    List<String> sources = buildSources(xyz, layers);
    return MessageFormat.format(SQL_LAYERS, JOIN_VALUES.join(values), JOIN_SOURCES.join(sources));
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

  protected static List<String> buildSources(XYZ xyz, List<PostgisLayer> layers) {
    List<String> sources = new ArrayList<>();
    for (PostgisLayer layer : layers) {
      sources.add(buildSource(xyz, layer));
    }
    return sources;
  }

  protected static String buildSource(XYZ xyz, PostgisLayer layer) {
    GeometryEnvelope bbox = xyz.envelope();
    String envelope =
        MessageFormat.format(
            SQL_ENVELOPE, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
    return MessageFormat.format(SQL_SOURCE, layer.getName(), layer.getSql(), envelope);
  }
}
