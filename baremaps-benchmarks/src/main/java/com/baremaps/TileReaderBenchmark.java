package com.baremaps;

import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.tiles.Tile;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.postgis.SlowTileReader;
import com.baremaps.tiles.postgis.FastTileReader;
import com.baremaps.tiles.util.TileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class TileReaderBenchmark {

  private Config config;
  private PoolingDataSource datasource;
  private TileReader reader;

  @Setup(Level.Invocation)
  public void prepare() throws IOException, ClassNotFoundException {
    Class.forName("org.postgresql.Driver");
    try (FileInputStream fis = new FileInputStream(new File("./examples/openstreetmap/config.yaml"))) {
      config = Config.load(fis);
      datasource = PostgisHelper.poolingDataSource(
          "jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps");
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 2)
  public void basic() throws SQLException, ParseException {
    reader = new SlowTileReader(datasource, config);
    execute();
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 2)
  public void with() throws SQLException, ParseException {
    reader = new FastTileReader(datasource, config);
    execute();
  }

  public void execute() throws SQLException, ParseException {
    try (Connection connection = datasource.getConnection()) {
      Geometry geometry = TileUtil.bbox(connection);
      Stream<Tile> coords = TileUtil.getTiles(geometry, 14, 14);
      coords.forEach(xyz -> {
        try {
          reader.read(xyz);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
  }


}
