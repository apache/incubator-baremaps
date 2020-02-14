package io.gazetteer.tiles.postgis;

import io.gazetteer.tiles.config.Layer;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

  private QueryParser() {

  }

  public static Query parse(Layer layer, String sql) {
    Pattern query = Pattern.compile("SELECT(.*?)FROM(.*?)(?:WHERE(.*))?");
    Matcher matcher = query.matcher(sql);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("The SQL query malformed");
    }

    String select = matcher.group(1).trim();
    String from = matcher.group(2).trim();
    Optional<String> where = Optional.ofNullable(matcher.group(3)).map(s -> s.trim());

    String[] columns = select.split(",(?![^()]*+\\))");
    if (columns.length != 3) {
      throw new IllegalArgumentException("The SQL query malformed");
    }

    String id = columns[0].trim();
    String tags = columns[1].trim();
    String geom = columns[2].trim();

    String source = "H" + Math.abs(Objects.hash(id, tags, geom, from));

    return new Query(layer, source, id, tags, geom, from, where);
  }

  public static class Query {

    private final Layer layer;
    private final String source;
    private final String id;
    private final String tags;
    private final String geom;
    private final String from;
    private final Optional<String> where;

    private Query(Layer layer, String source, String id, String tags, String geom, String from, Optional<String> where) {
      this.layer = layer;
      this.source = source;
      this.id = id;
      this.tags = tags;
      this.geom = geom;
      this.from = from;
      this.where = where;
    }

    public Layer getLayer() {
      return layer;
    }

    public String getSource() {
      return source;
    }

    public String getId() {
      return id;
    }

    public String getTags() {
      return tags;
    }

    public String getGeom() {
      return geom;
    }

    public String getFrom() {
      return from;
    }

    public Optional<String> getWhere() {
      return where;
    }
  }

}
