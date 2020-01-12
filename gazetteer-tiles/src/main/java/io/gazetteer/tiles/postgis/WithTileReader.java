package io.gazetteer.tiles.postgis;

import com.google.common.base.Stopwatch;
import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.config.Config;
import io.gazetteer.tiles.config.Layer;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.proj4j.*;

public class WithTileReader extends AbstractTileReader {

  private class Select {

    private final String id;
    private final String tags;
    private final String geom;
    private final String tables;
    private final Optional<String> conditions;

    private Select(String id, String tags, String geom, String tables, String conditions) {
      this.id = id;
      this.tags = tags;
      this.geom = geom;
      this.tables = tables;
      this.conditions = Optional.ofNullable(conditions);
    }

    public String source() {
      return "H" + Math.abs(Objects.hash(id, tags, geom, tables));
    }

  }

  private static final String SQL_WITH = "WITH {0} {1}";

  private static final String SQL_SOURCE = "{0} AS (SELECT {1}, "
      + "({2} || hstore(''geometry'', lower(replace(st_geometrytype({3}), ''ST_'', '''')))) as {2}, "
      + "ST_AsMvtGeom({3}, {5}, 4096, 256, true) AS {3} "
      + "FROM {4} WHERE ST_Intersects({3}, {5}))";

  private static final String SQL_LAYER = "SELECT ST_AsMVT(mvt_geom, ''{0}'', 4096) FROM ({1}) as mvt_geom";

  private static final String SQL_QUERY = "SELECT {0}, {1}::jsonb, {2} FROM {3}";

  private static final String SQL_CONDITION = " WHERE {0}";

  private static final String SQL_COMMA = ", ";

  private static final String SQL_UNION = " UNION All ";

  private final PoolingDataSource datasource;

  private final Config config;

  public WithTileReader(PoolingDataSource datasource, Config config) {
    this.datasource = datasource;
    this.config = config;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try (Connection connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(data)) {
      String sql = query(tile, config.getLayers());
      try (Statement statement = connection.createStatement()) {
        ResultSet result = statement.executeQuery(sql);
        while (result.next()) {
          gzip.write(result.getBytes(1));
        }
      }
      gzip.close();
      return data.toByteArray();
    } catch (Exception e) {
      throw new TileException(e);
    }

  }

  private String query(Tile tile, List<Layer> layers) {
    String sources = layers.stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> query.getSql()))
        .map(this::parse)
        .map(select -> MessageFormat.format(SQL_SOURCE,
            select.source(),
            select.id,
            select.tags,
            select.geom,
            select.tables,
            envelope(tile)))
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.joining(SQL_COMMA));
    String targets = layers.stream()
        .map(layer -> {
          String queries = layer.getQueries().stream()
              .map(query -> query.getSql())
              .map(this::parse)
              .map(select -> {
                String l = MessageFormat.format(SQL_QUERY, select.id, select.tags, select.geom, select.source());
                String r = select.conditions
                    .map(conditions -> MessageFormat.format(SQL_CONDITION, conditions))
                    .orElse("");
                return l + r;
              })
              .collect(Collectors.joining(SQL_UNION));
          return MessageFormat.format(SQL_LAYER, layer.getName(), queries);
        })
        .collect(Collectors.joining(SQL_UNION));
    return MessageFormat.format(SQL_WITH, sources, targets);
  }


  private Select parse(String sql) {
    Matcher matcher = SQL_SELECT.matcher(sql);
    if (matcher.matches()) {
      String id = matcher.group(1).trim();
      String tags = matcher.group(2).trim();
      String geom = matcher.group(3).trim();
      String tables = matcher.group(4).trim();
      String conditions = matcher.group(5);
      return new Select(id, tags, geom, tables, conditions);
    } else {
      throw new UnsupportedOperationException();
    }
  }

}
