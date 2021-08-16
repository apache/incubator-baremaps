package com.baremaps.tile.postgres;

import static com.baremaps.testing.TestConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresQueryGeneratorTest {

  @Test
  @Tag("integration")
  void generate() throws SQLException, IOException {
    DataSource dataSource = PostgresUtils.datasource(DATABASE_URL);

    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }

    List<PostgresQuery> queries = new PostgresQueryGenerator(
        dataSource,
        null,
        "public",
        null,
        null,
        "TABLE"
    ).generate();

    assertEquals(3, queries.size());
    assertEquals(
        "SELECT id, hstore(array['version', version::text, 'uid', uid::text, 'timestamp', timestamp::text, 'changeset', changeset::text, 'tags', tags::text, 'lon', lon::text, 'lat', lat::text]), geom FROM osm_nodes",
        queries.get(0).getSql());
  }

}