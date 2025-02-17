package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RelationBoundaryBuilder implements Consumer<Entity> {

	private static final Logger logger = LoggerFactory.getLogger(RelationBoundaryBuilder.class);

	private final Map<Long, Node> nodes;
	private final Map<Long, Way> ways;
	private final Map<Long, Relation> relations;
	private final GeometryFactory geometryFactory;

	public RelationBoundaryBuilder(
		Map<Long, Node> nodes,
		Map<Long, Way> ways,
		Map<Long, Relation> relations,
		GeometryFactory geometryFactory
	) {
		this.nodes = nodes;
		this.ways = ways;
		this.relations = relations;
		this.geometryFactory = geometryFactory;
	}

	@Override
	public void accept(final Entity entity) {
		if (entity instanceof Relation relation) {
			try {
				var start = System.currentTimeMillis();

				buildBoundary(relation);

				var end = System.currentTimeMillis();
				var duration = end - start;
				if (duration > 60 * 1000) {
					logger.debug("Relation #{} processed in {} ms", relation.getId(), duration);
				}
			} catch (Exception e) {
				logger.error("Error processing relation #" + relation.getId(), e);
			}
		}
	}

	public Geometry buildBoundary(Relation relation) {
		List<Geometry> geometries = relation.getMembers().stream()
			.map(member -> switch (member.type()) {
				case NODE -> nodes.get(member.ref()).getGeometry();
				case WAY -> ways.get(member.ref()).getGeometry();
				case RELATION -> buildBoundary(relations.get(member.ref()));
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		GeometryCombiner combiner = new GeometryCombiner(geometries);
		Geometry combinedGeometry = combiner.combine();

		if (combinedGeometry instanceof MultiPolygon || combinedGeometry instanceof MultiLineString) {
			return combinedGeometry;
		} else {
			return geometryFactory.createGeometryCollection(new Geometry[]{combinedGeometry});
		}
	}
}