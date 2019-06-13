package io.gazetteer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.postgis.EntityConsumer;
import io.gazetteer.osm.postgis.DatabaseUtil;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.dbcp2.PoolingDataSource;
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
  public void prepare() throws SQLException, IOException {
    try (Connection connection = DriverManager.getConnection(POSTGRES_URL)) {
      URL url = Resources.getResource("osm_create_tables.sql");
      String sql = Resources.toString(url, Charsets.UTF_8);
      connection.createStatement().execute(sql);
    }
    PoolingDataSource pool = DatabaseUtil.createPoolingDataSource(POSTGRES_URL);
    consumer = new EntityConsumer(pool);
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
