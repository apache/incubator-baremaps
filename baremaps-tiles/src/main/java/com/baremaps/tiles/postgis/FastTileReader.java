package com.baremaps.tiles.postgis;

import com.baremaps.tiles.postgis.QueryParser.Query;
import com.baremaps.tiles.Tile;
import com.baremaps.tiles.TileException;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.config.Layer;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FastTileReader extends AbstractTileReader {

  private static Logger logger = LogManager.getLogger();

  private static final String WITH = "WITH {0} {1}";

  private static final String SOURCE = "{0} AS (SELECT id, "
      + "(tags || hstore(''geometry'', lower(replace(st_geometrytype(geom), ''ST_'', '''')))) as tags, "
      + "ST_AsMvtGeom(geom, {5}, 4096, 256, true) AS geom "
      + "FROM (SELECT {1} as id, {2} as tags, {3} as geom FROM {4}) AS {0} WHERE ST_Intersects(geom, {5}))";

  private static final String LAYER = "SELECT ST_AsMVT(mvt_geom, ''{0}'', 4096) FROM ({1}) as mvt_geom";

  private static final String QUERY = "SELECT id, tags::jsonb, geom FROM {3}";

  private static final String WHERE = " WHERE {0}";

  private static final String COMMA = ", ";

  private static final String UNION_ALL = " UNION All ";

  private final PoolingDataSource datasource;

  private final Config config;

  private final Map<Layer, List<Query>> queries;

  public FastTileReader(PoolingDataSource datasource, Config config) {
    this.datasource = datasource;
    this.config = config;
    this.queries = config.getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> QueryParser.parse(layer, query.getSql())))
        .collect(Collectors.groupingBy(q -> q.getLayer()));
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try (Connection connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(data)) {
      try (Statement statement = connection.createStatement()) {
        String sql = query(tile);
        logger.debug("Executing tile query: {}", sql);
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

  private String query(Tile tile) {
    String sources = queries.entrySet().stream()
        .filter(entry -> entry.getKey().getMinZoom() <= tile.getZ() && entry.getKey().getMaxZoom() >= tile.getZ())
        .flatMap(entry -> entry.getValue().stream().map(query -> MessageFormat.format(SOURCE,
            query.getSource(),
            query.getId(),
            query.getTags(),
            query.getGeom(),
            query.getFrom(),
            envelope(tile))))
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.joining(COMMA));
    String targets = queries.entrySet().stream()
        .filter(entry -> entry.getKey().getMinZoom() <= tile.getZ() && entry.getKey().getMaxZoom() >= tile.getZ())
        .map(entry -> {
          String queries = entry.getValue().stream()
              .map(select -> {
                String l = MessageFormat.format(QUERY,
                    select.getId(),
                    select.getTags(),
                    select.getGeom(),
                    select.getSource());
                String r = select.getWhere()
                    .map(s -> MessageFormat.format(WHERE, s))
                    .orElse("");
                return l + r;
              })
              .collect(Collectors.joining(UNION_ALL));
          return MessageFormat.format(LAYER, entry.getKey().getName(), queries);
        })
        .collect(Collectors.joining(UNION_ALL));
    return MessageFormat.format(WITH, sources, targets);
  }

}