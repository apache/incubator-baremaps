package io.gazetteer.osm.lmdb;

import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

public class LmdbUtil {

  public static LmdbConsumer consumer(Path lmdbPath) {
    final Env<ByteBuffer> env =
        Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(lmdbPath.toFile());
    final LmdbStore<Long, Node> nodes =
        new LmdbStore<>(env, env.openDbi("nodes", MDB_CREATE), new NodeType());
    final LmdbStore<Long, Way> ways =
        new LmdbStore<>(env, env.openDbi("ways", MDB_CREATE), new WayType());
    final LmdbStore<Long, Relation> relations =
        new LmdbStore<>(env, env.openDbi("ways", MDB_CREATE), new RelationType());
    return new LmdbConsumer(nodes, ways, relations);
  }
}
