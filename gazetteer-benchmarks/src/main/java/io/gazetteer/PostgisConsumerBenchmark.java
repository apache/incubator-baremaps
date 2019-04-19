package io.gazetteer;

import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.pgbulkinsert.PgBulkInsertUtil;
import io.gazetteer.osm.postgis.PostgisSchema;
import org.openjdk.jmh.annotations.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.gazetteer.Constants.PBF_FILE;
import static io.gazetteer.Constants.POSTGRES_URL;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class PostgisConsumerBenchmark {

  public Stream<DataBlock> stream;

  public Consumer consumer;

  @Setup(Level.Invocation)
  public void prepare() throws SQLException {
    try (Connection connection = DriverManager.getConnection(POSTGRES_URL)) {
      PostgisSchema.createExtensions(connection);
      PostgisSchema.dropIndices(connection);
      PostgisSchema.dropTables(connection);
      PostgisSchema.createTables(connection);
    }
    consumer = PgBulkInsertUtil.consumer(POSTGRES_URL);
    InputStream input = getClass().getClassLoader().getResourceAsStream(PBF_FILE);
    stream = PBFUtil.dataBlocks(input);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void processStream() throws ExecutionException, InterruptedException {
    ForkJoinPool executor = new ForkJoinPool(1);
    executor.submit(() -> stream.forEach(consumer)).get();
  }
}
