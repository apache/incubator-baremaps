/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.exporter.store;

import com.baremaps.exporter.config.Config;
import com.baremaps.exporter.config.Layer;
import com.baremaps.exporter.store.PostgisQueryParser.Parse;
import com.baremaps.util.tile.Tile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.inject.Provider;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class PostgisTileStore implements TileStore {

  public static final String BBOX = "SELECT st_asewkb(st_transform(st_setsrid(st_extent(geom), 3857), 4326)) as table_extent FROM osm_nodes";

  private static final Logger logger = LogManager.getLogger();

  private static final String WITH = "WITH %1$s %2$s";

  private static final String SOURCE = "%1$s AS (SELECT id, "
      + "(tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, "
      + "ST_AsMvtGeom(geom, %6$s, 4096, 256, true) AS geom "
      + "FROM (SELECT %2$s as id, %3$s as tags, %4$s as geom FROM %5$s) AS q WHERE ST_Intersects(geom, %6$s))";

  private static final String LAYER = "SELECT ST_AsMVT(mvt_geom, '%1$s', 4096) FROM (%2$s) as mvt_geom";

  private static final String QUERY = "SELECT id, hstore_to_jsonb_loose(tags), geom FROM %1$s";

  private static final String WHERE = " WHERE %1$s";

  private static final String COMMA = ", ";

  private static final String UNION_ALL = " UNION All ";

  private static final String ENVELOPE = "ST_MakeEnvelope(%1$s, %2$s, %3$s, %4$s, 3857)";

  private final CRSFactory crsFactory = new CRSFactory();

  private final CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");

  private final CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");

  private final CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();

  private final CoordinateTransform coordinateTransform = coordinateTransformFactory
      .createTransform(epsg4326, epsg3857);

  private final DataSource datasource;

  private final Provider<Config> provider;

  public PostgisTileStore(DataSource datasource, Provider<Config> provider) {
    this.datasource = datasource;
    this.provider = provider;

  }

  public Envelope envelope() throws SQLException, ParseException {
    try (Connection connection = datasource.getConnection();
        PreparedStatement statement = connection.prepareStatement(BBOX)) {
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return new WKBReader().read(result.getBytes(1)).getEnvelopeInternal();
      } else {
        return null;
      }
    }
  }

  public byte[] read(Tile tile) throws TileStoreException {
    try (Connection connection = datasource.getConnection()) {
      try (Statement statement = connection.createStatement();
          ByteArrayOutputStream data = new ByteArrayOutputStream()) {

        String sql = query(tile);

        logger.debug("Executing tile query: {}", sql);
        ResultSet resultSet = statement.executeQuery(sql);

        int length = 0;
        GZIPOutputStream gzip = new GZIPOutputStream(data);
        while (resultSet.next()) {
          byte[] bytes = resultSet.getBytes(1);
          length += bytes.length;
          gzip.write(bytes);
        }
        gzip.close();

        if (length > 0) {
          return data.toByteArray();
        } else {
          return null;
        }
      }
    } catch (SQLException | IOException e) {
      throw new TileStoreException(e);
    }
  }

  private String query(Tile tile) {
    Map<Layer, List<Parse>> parses = provider.get().getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> PostgisQueryParser.parse(layer, query)))
        .collect(Collectors.groupingBy(q -> q.getLayer()));
    String sources = parses.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .filter(parse -> zoomFilter(tile, parse))
            .map(parse -> String.format(
                SOURCE,
                parse.getSource(),
                parse.getId(),
                parse.getTags(),
                parse.getGeom(),
                parse.getFrom(),
                envelope(tile))))
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.joining(COMMA));
    String targets = parses.entrySet().stream()
        .filter(entry -> entry.getValue().stream()
            .filter(parse -> zoomFilter(tile, parse))
            .count() > 0)
        .map(entry -> String.format(
            LAYER,
            entry.getKey().getId(),
            entry.getValue().stream()
                .filter(parse -> zoomFilter(tile, parse))
                .map(parse -> new StringBuilder()
                    .append(String.format(QUERY, parse.getSource()))
                    .append(parse.getWhere()
                        .map(s -> String.format(WHERE, s))
                        .orElse(""))
                    .toString())
                .collect(Collectors.joining(UNION_ALL))))
        .collect(Collectors.joining(UNION_ALL));
    String query = String.format(WITH, sources, targets)
        .replace("${zoom}", String.valueOf(tile.z()));
    return query;
  }

  private boolean zoomFilter(Tile tile, Parse parse) {
    return parse.getQuery().getMinZoom() <= tile.z() && tile.z() < parse.getQuery().getMaxZoom();
  }

  private String envelope(Tile tile) {
    Envelope envelope = tile.envelope();
    Coordinate min = coordinate(envelope.getMinX(), envelope.getMinY());
    Coordinate max = coordinate(envelope.getMaxX(), envelope.getMaxY());
    return String.format(
        ENVELOPE,
        min.getX(),
        min.getY(),
        max.getX(),
        max.getY());
  }

  private Coordinate coordinate(double x, double y) {
    ProjCoordinate coordinate = coordinateTransform.transform(new ProjCoordinate(x, y), new ProjCoordinate());
    return new Coordinate(coordinate.x, coordinate.y);
  }

  public void write(Tile tile, byte[] bytes) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

  public void delete(Tile tile) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

}