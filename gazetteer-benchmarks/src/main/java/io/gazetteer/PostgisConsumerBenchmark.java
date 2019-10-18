package io.gazetteer;

import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class PostgisConsumerBenchmark {

  public Stream<PrimitiveBlock> stream;

  public Consumer consumer;
  /*
  @Setup(Level.Invocation)
  public void prepare() throws SQLException, IOException {
    try (Connection connection = DriverManager.getConnection(POSTGRES_URL)) {
      URL url = Resources.getResource("osm_create_tables.sql");
      String sql = Resources.toString(url, Charsets.UTF_8);
      connection.createStatement().execute(sql);
    }
    PoolingDataSource pool = DatabaseUtils.poolingDataSource(POSTGRES_URL);
    Cache<Coordinate> coordinateMap = new Cache<Coordinate>(new CoordinateMapper());
    consumer = new BlockConsumer(pool, coordinateMap);
    InputStream input = Files.newInputStream(Paths.get(PBF_FILE));
    stream = PBFUtil.toPrimitiveBlock(PBFUtil.stream(input));
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void processStream() throws ExecutionException, InterruptedException {
    ForkJoinPool executor = new ForkJoinPool(1);
    executor.submit(() -> stream.forEach(consumer)).get();
  }
  */
}
