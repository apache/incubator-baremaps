package org.apache.baremaps.collection.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnionStreamTest {

    @org.junit.jupiter.api.Test
    void union() {
        var factory = new GeometryFactory();
        var polygon1 = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        });
        var polygon2 = factory.createPolygon(new Coordinate[]{
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 1),
                new Coordinate(2, 0),
                new Coordinate(1, 0)
        });
        var polygon3 = factory.createPolygon(new Coordinate[]{
                new Coordinate(2, 0),
                new Coordinate(2, 1),
                new Coordinate(3, 1),
                new Coordinate(3, 0),
                new Coordinate(2, 0)
        });
        var polygon4 = factory.createPolygon(new Coordinate[]{
                new Coordinate(4, 0),
                new Coordinate(4, 1),
                new Coordinate(5, 1),
                new Coordinate(5, 0),
                new Coordinate(4, 0)
        });
        List<Geometry> list = List.of(polygon1, polygon2, polygon3, polygon4);
        var union = new UnionStream(list).union().toList();
        assertEquals(union.size(), 2);
        assertEquals(union.get(0), UnaryUnionOp.union(List.of(polygon1, polygon2, polygon3)));
        assertEquals(union.get(1), polygon4);

    }

}