package io.gazetteer.tiles.postgis;

import io.gazetteer.tiles.Tile;
import io.gazetteer.tiles.TileException;
import io.gazetteer.tiles.config.Config;
import io.gazetteer.tiles.config.Layer;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.dbcp2.PoolingDataSource;

public class SimpleTileReader extends AbstractTileReader {

  private static final String SQL_LAYER = "SELECT ST_AsMVT(mvt_geom, ''{0}'', 4096, ''geom'') FROM {1}";

  private static final String SQL_QUERY = "SELECT {0} AS id, {1} AS tags, {2} AS geom FROM {3}{4}";

  private static final String SQL_WHERE = " WHERE {0}";

  private static final String SQL_SOURCE =
      "(SELECT id, "
          + "(tags || hstore(''geometry'', lower(replace(st_geometrytype(geom), ''ST_'', ''''))))::jsonb, "
          + "ST_AsMvtGeom(geom, {2}, 4096, 256, true) AS geom "
          + "FROM ({1}) AS layer "
          + "WHERE ST_Intersects(geom, {2})"
          + ") as mvt_geom";

  private static final CharSequence SQL_UNION_ALL = " UNION ALL ";

  private final PoolingDataSource datasource;

  private final Config config;

  public SimpleTileReader(PoolingDataSource datasource, Config config) {
    this.datasource = datasource;
    this.config = config;
  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try (Connection connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(data)) {
      for (Layer layer : config.getLayers()) {
        if (tile.getZ() >= layer.getMinZoom() && tile.getZ() <= layer.getMaxZoom()) {
          String sql = MessageFormat.format(SQL_LAYER,
              layer.getName(),
              MessageFormat.format(SQL_SOURCE,
                  layer.getName(),
                  layer.getQueries().stream()
                      .map(query -> QueryParser.parse(layer, query.getSql()))
                      .map(query -> MessageFormat.format(SQL_QUERY,
                          query.getId(),
                          query.getTags(),
                          query.getGeom(),
                          query.getFrom(),
                          query.getWhere().map(where -> MessageFormat.format(SQL_WHERE, where))
                              .orElse("")))
                      .collect(Collectors.joining(SQL_UNION_ALL)),
                  envelope(tile)));
          try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(sql);
            if (result.next()) {
              gzip.write(result.getBytes(1));
            }
          }
        }
      }
      gzip.close();
      return data.toByteArray();
    } catch (Exception e) {
      throw new TileException(e);
    }
  }

}
