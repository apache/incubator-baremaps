package io.gazetteer;

import io.gazetteer.osm.lmdb.*;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import org.lmdbjava.Env;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

public class OsmBenchmark {

  @Benchmark
  public void testMethod() throws IOException, ExecutionException, InterruptedException {
    Path directory = Files.createTempDirectory("benchmarks_");
    final Env<ByteBuffer> env =
        Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(directory.toFile());
    final LmdbStore<Long, Node> nodes =
        new LmdbStore<>(env, env.openDbi("nodes", MDB_CREATE), new NodeType());
    final LmdbStore<Long, Way> ways =
        new LmdbStore<>(env, env.openDbi("ways", MDB_CREATE), new WayType());
    final LmdbStore<Long, Relation> relations =
        new LmdbStore<>(env, env.openDbi("ways", MDB_CREATE), new RelationType());
    LmdbConsumer lmdbConsumer = new LmdbConsumer(nodes, ways, relations);
    InputStream input = getClass().getClassLoader().getResourceAsStream("liechtenstein.osm.pbf");
    Stream<DataBlock> lmdbStream = PBFUtil.dataBlocks(input);
    ForkJoinPool executor = new ForkJoinPool(1);
    executor.submit(() -> lmdbStream.forEach(lmdbConsumer)).get();
  }
}
