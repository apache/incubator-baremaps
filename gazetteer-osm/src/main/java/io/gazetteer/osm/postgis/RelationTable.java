package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Relation;
import io.gazetteer.postgis.util.CopyWriter;
import java.util.List;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class RelationTable {

  private static final String COPY_RELATIONS = "COPY osm_relations (id, version, uid, timestamp, changeset, tags, nodes) FROM STDIN BINARY";

  public static void copyRelations(PGConnection connection, List<Relation> relations) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_RELATIONS))) {
      writer.writeHeader();
      for (Relation relation : relations) {
        writer.startRow(7);
        writer.writeLong(relation.getInfo().getId());
        writer.writeInteger(relation.getInfo().getVersion());
        writer.writeInteger(relation.getInfo().getUserId());
        writer.writeLong(relation.getInfo().getTimestamp());
        writer.writeLong(relation.getInfo().getChangeset());
        writer.writeHstore(relation.getInfo().getTags());
        //writer.writeLongList(relation.getMembers().stream().map());
      }
    }
  }

}
