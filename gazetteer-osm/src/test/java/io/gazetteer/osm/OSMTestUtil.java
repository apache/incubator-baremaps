package io.gazetteer.osm;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import io.gazetteer.osm.geometry.NodeBuilder;
import io.gazetteer.osm.model.*;
import io.gazetteer.osm.osmpbf.FileBlock;
import io.gazetteer.osm.osmpbf.FileBlock.Type;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import io.gazetteer.osm.store.StoreReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;

public class OSMTestUtil {

    private static final CRSFactory CRS_FACTORY = new CRSFactory();

    private static final CoordinateReferenceSystem EPSG_4326 = CRS_FACTORY.createFromName("EPSG:4326");

    private static final CoordinateTransformFactory COORDINATE_TRANSFORM_FACTORY = new CoordinateTransformFactory();

    public static final CoordinateTransform COORDINATE_TRANSFORM = COORDINATE_TRANSFORM_FACTORY.createTransform(EPSG_4326, EPSG_4326);

    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    public static final NodeBuilder GEOMETRY_BUILDER = new NodeBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY);

    public static final LocalDateTime TIMESTAMP = LocalDateTime.of(2020, 1, 1, 0, 0);

    public static final Map<String, String> TAGS = new HashMap<>();

    public static final Map<String, String> TAGS_RELATION = ImmutableMap.of("type", "multipolygon");

    public static final Info INFO_0 = new Info(0, 0, TIMESTAMP, 0, 0, TAGS);

    public static final Info INFO_1 = new Info(1, 0, TIMESTAMP, 0, 0, TAGS_RELATION);

    public static final Node NODE_0 = new Node(INFO_0, 0, 0);

    public static final Node NODE_1 = new Node(INFO_0, 0, 3);

    public static final Node NODE_2 = new Node(INFO_0, 3, 3);

    public static final Node NODE_3 = new Node(INFO_0, 3, 0);

    public static final Node NODE_4 = new Node(INFO_0, 1, 1);

    public static final Node NODE_5 = new Node(INFO_0, 1, 2);

    public static final Node NODE_6 = new Node(INFO_0, 2, 2);

    public static final Node NODE_7 = new Node(INFO_0, 2, 1);

    public static final Node NODE_8 = new Node(INFO_0, 4, 1);

    public static final Node NODE_9 = new Node(INFO_0, 4, 2);

    public static final Node NODE_10 = new Node(INFO_0, 5, 2);

    public static final Node NODE_11 = new Node(INFO_0, 5, 1);

    public static final List<Node> NODE_LIST = Arrays.asList(NODE_0, NODE_1, NODE_2, NODE_3, NODE_4, NODE_5, NODE_6, NODE_7, NODE_8, NODE_9, NODE_10, NODE_11);

    public static final StoreReader<Long, Coordinate> COORDINATE_STORE = new StoreReader<Long, Coordinate>() {
        @Override
        public Coordinate get(Long key) {
            Node node = NODE_LIST.get(key.intValue());
            return new Coordinate(node.getLon(), node.getLat());
        }

        @Override
        public List<Coordinate> getAll(List<Long> keys) {
            List<Coordinate> coordinateList = new ArrayList<>();
            for (Long key : keys) coordinateList.add(get(key));
            return coordinateList;
        }
    };

    public static final Way WAY_LINESTRING = new Way(INFO_0, Arrays.asList(0l, 1l, 2l, 3l));

    public static final Way WAY_POLYGON_OUTER_1 = new Way(INFO_0, Arrays.asList(0l, 1l, 2l, 3l, 0l));

    public static final Way WAY_POLYGON_OUTER_2 = new Way(INFO_0, Arrays.asList(8l, 9l, 10l, 11l, 8l));

    public static final Way WAY_POLYGON_INNER_1 = new Way(INFO_0, Arrays.asList(4l, 5l, 6l, 7l, 4l));

    public static final List<Way> WAY_LIST = Arrays.asList(WAY_POLYGON_OUTER_1, WAY_POLYGON_INNER_1, WAY_POLYGON_OUTER_2);

    public static final StoreReader<Long, List<Long>> REFERENCE_STORE = new StoreReader<Long, List<Long>>() {
        @Override
        public List<Long> get(Long key) {
            return WAY_LIST.get(key.intValue()).getNodes();
        }

        @Override
        public List<List<Long>> getAll(List<Long> keys) {
            List<List<Long>> referenceList = new ArrayList<>();
            for (Long key : keys) referenceList.add(get(key));
            return referenceList;
        }
    };

    public static final Relation RELATION_0 = new Relation(INFO_0, Arrays.asList(
            new Member(0, Member.Type.way, "outer")));

    public static final Relation RELATION_1 = new Relation(INFO_1, Arrays.asList(
            new Member(0, Member.Type.way, "outer"),
            new Member(1, Member.Type.way, "inner")));

    public static final Relation RELATION_2 = new Relation(INFO_1, Arrays.asList(
            new Member(0, Member.Type.way, "outer"),
            new Member(1, Member.Type.way, "inner"),
            new Member(2, Member.Type.way, "outer")));

    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/osm?allowMultiQueries=true&user=osm&password=osm";

    public static InputStream dataOsmPbf() {
        return OSMTestUtil.class.getClassLoader().getResourceAsStream("data.osm.pbf");
    }

    public static InputStream denseOsmPbf() {
        return OSMTestUtil.class.getClassLoader().getResourceAsStream("dense.osm.pbf");
    }

    public static InputStream waysOsmPbf() {
        return OSMTestUtil.class.getClassLoader().getResourceAsStream("ways.osm.pbf");
    }

    public static InputStream relationsOsmPbf() {
        return OSMTestUtil.class.getClassLoader().getResourceAsStream("relations.osm.pbf");
    }

    public static FileBlock invalidOsmPbf() {
        return new FileBlock(Type.OSMHeader, ByteString.copyFromUtf8(""), ByteString.copyFromUtf8(""));
    }

    public static InputStream dataOsmXml() {
        return OSMTestUtil.class.getClassLoader().getResourceAsStream("data.osm.xml");
    }

    public static InputStream dataOscXml() {
        return OSMTestUtil.class.getClassLoader().getResourceAsStream("data.osc.xml");
    }

}
