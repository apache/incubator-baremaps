package com.baremaps.importer;

import static com.baremaps.importer.database.DatabaseConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.importer.cache.InMemoryCache;
import com.baremaps.importer.cache.PostgisCoordinateCache;
import com.baremaps.importer.cache.PostgisReferenceCache;
import com.baremaps.importer.database.DatabaseException;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.importer.geometry.ProjectionTransformer;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.util.postgis.PostgisHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

class ImportUpdateTest {

  public PoolingDataSource dataSource;
  public HeaderTable headerTable;
  public NodeTable nodeTable;
  public WayTable wayTable;
  public RelationTable relationTable;
  public DataImporter importer;
  public DataUpdater updater;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    GeometryFactory source = new GeometryFactory(new PrecisionModel(), 4326);
    GeometryFactory target = new GeometryFactory(new PrecisionModel(), 3857);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(source, target);

    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    headerTable = new HeaderTable(dataSource);
    nodeTable = new NodeTable(dataSource);
    wayTable = new WayTable(dataSource);
    relationTable = new RelationTable(dataSource);

    importer = new DataImporter(
        projectionTransformer,
        new InMemoryCache<>(),
        new InMemoryCache<>(),
        headerTable,
        nodeTable,
        wayTable,
        relationTable
    );

    updater = new DataUpdater(
        projectionTransformer,
        new PostgisCoordinateCache(dataSource),
        new PostgisReferenceCache(dataSource),
        headerTable,
        nodeTable,
        wayTable,
        relationTable
    );

    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.execute(connection, "osm_create_extensions.sql");
      PostgisHelper.execute(connection, "osm_drop_tables.sql");
      PostgisHelper.execute(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void test() throws IOException, DatabaseException, URISyntaxException {
    Node node;
    Way way;
    Relation relation;

    // Import data
    importer.execute(TestFiles.dataOsmXml());

    // Check node importation
    assertNull(nodeTable.select(0l));
    assertNotNull(nodeTable.select(1l));
    assertNotNull(nodeTable.select(2l));
    assertNotNull(nodeTable.select(3l));
    assertNull(nodeTable.select(4l));

    // Check way importation
    assertNull(wayTable.select(0l));
    assertNotNull(wayTable.select(1l));
    assertNull(wayTable.select(2l));

    // Check relation importation
    assertNull(relationTable.select(0l));
    assertNotNull(relationTable.select(1l));
    assertNull(relationTable.select(2l));

    // Check node properties
    node = nodeTable.select(1l);
    assertEquals(1, node.getLon());
    assertEquals(1, node.getLat());

    // Check way properties
    way = wayTable.select(1l);
    assertNotNull(way);

    // Update data
    updater.execute(TestFiles.dataOscXml());

    // Check modifications
    node = nodeTable.select(1l);
    assertNull(node);

    node = nodeTable.select(2l);
    assertNotNull(node);
    assertEquals(1, node.getLon());
    assertEquals(1, node.getLat());

    node = nodeTable.select(4l);
    assertNotNull(node);
    assertEquals(1, node.getLon());
    assertEquals(4, node.getLat());
  }

}