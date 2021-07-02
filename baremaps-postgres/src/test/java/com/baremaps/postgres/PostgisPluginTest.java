package com.baremaps.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.postgres.jdbi.PostgisPlugin;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class PostgisPluginTest {

  @Test
  @Tag("integration")
  void test() {
    Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps")
        .installPlugin(new PostgisPlugin());

    PostgisRecord record = new PostgisRecord();
    record.setId(1);
    record.setPoint(new GeometryFactory().createPoint(new Coordinate(1, 1)));
    record.setLineString(new GeometryFactory().createLineString(new Coordinate[]{
        new Coordinate(1, 1),
        new Coordinate(1, 2),
        new Coordinate(2, 2),
        new Coordinate(2, 1)
    }));
    record.setPolygon(new GeometryFactory().createPolygon(new Coordinate[]{
        new Coordinate(1, 1),
        new Coordinate(1, 2),
        new Coordinate(2, 2),
        new Coordinate(2, 1),
        new Coordinate(1, 1),
    }));

    List<PostgisRecord> result = jdbi.withHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS record");
      handle.execute(
          "CREATE TABLE record (id INTEGER PRIMARY KEY, point geometry(point), linestring geometry(linestring), polygon geometry(polygon))");
      handle.createUpdate(
          "INSERT INTO record (id, point, linestring, polygon) VALUES (:id, :point, :lineString, :polygon)")
          .bindBean(record)
          .execute();
      return handle.createQuery("SELECT * FROM record ORDER BY id")
          .mapToBean(PostgisRecord.class)
          .list();
    });

    assertEquals(record.getPoint(), result.get(0).getPoint());
    assertEquals(record.getLineString(), result.get(0).getLineString());
    assertEquals(record.getPolygon(), result.get(0).getPolygon());
  }

}