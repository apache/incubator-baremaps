package org.apache.baremaps.workflow.tasks;

import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public record ImportDaylightTranslations(Path file, Object database) implements Task {

    record Group(String type, Long id, String name) {

    }

    record Line(String type, Long id, String name, String attributeKey, String attributeValue) {

        public Group group() {
            return new Group(type, id, name);
        }

        public static Line parse(String line) {
            var parts = line.split("\t");
            var type = parts[0];
            var id = Long.parseLong(parts[1]);
            var name = parts[2];
            var key = parts[3];
            var val = parts[4];
            return new Line(type, id, name, key, val);
        }
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        var datasource = context.getDataSource(database);
        var nodeRepository = new PostgresNodeRepository(datasource);
        var wayRepository = new PostgresWayRepository(datasource);
        var relationRepository = new PostgresRelationRepository(datasource);
        nodeRepository.create();
        wayRepository.create();
        relationRepository.create();
        try (var lines = Files.lines(file)) {
            var entries = lines.map(Line::parse).collect(Collectors.groupingBy(Line::group));
            for (var entry : entries.entrySet()) {
                var group = entry.getKey();
                switch (group.type()) {
                    case "node" -> {
                        var node = nodeRepository.get(group.id());
                        if (node != null) {
                            var tags = node.getTags();
                            for (var line : entry.getValue()) {
                                tags.put(line.attributeKey(), line.attributeValue());
                            }
                            nodeRepository.put(node);
                        }
                    }
                    case "way" -> {
                        var way = wayRepository.get(group.id());
                        if (way != null) {
                            var tags = way.getTags();
                            for (var line : entry.getValue()) {
                                tags.put(line.attributeKey(), line.attributeValue());
                            }
                            wayRepository.put(way);
                        }
                    }
                    case "relation" -> {
                        var relation = relationRepository.get(group.id());
                        if (relation != null) {
                            var tags = relation.getTags();
                            for (var line : entry.getValue()) {
                                tags.put(line.attributeKey(), line.attributeValue());
                            }
                            relationRepository.put(relation);
                        }
                    }
                }
            }
        }
    }
}
