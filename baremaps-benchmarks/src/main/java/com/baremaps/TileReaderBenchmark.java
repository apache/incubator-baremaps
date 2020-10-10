/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps;

import com.baremaps.exporter.config.Config;
import com.baremaps.exporter.config.Loader;
import com.baremaps.exporter.store.PostgisTileStore;
import com.baremaps.exporter.store.TileStoreException;
import com.baremaps.util.postgis.PostgisHelper;
import com.baremaps.util.storage.LocalBlobStore;
import com.baremaps.util.tile.Tile;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Envelope;
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
  private PostgisTileStore reader;

  @Setup(Level.Invocation)
  public void prepare() throws IOException, ClassNotFoundException {
    Class.forName("org.postgresql.Driver");
    URI uri = Paths.get("./examples/openstreetmap/config.yaml").toUri();
    Loader loader = new Loader(new LocalBlobStore());
    Config config = loader.load(uri);
    datasource = PostgisHelper.poolingDataSource(
        "jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps");
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 2)
  public void with() throws SQLException, ParseException {
    reader = new PostgisTileStore(datasource, () -> config);
    execute();
  }

  public void execute() throws SQLException, ParseException {
    Envelope geometry = reader.envelope();
    Stream<Tile> coords = Tile.getTiles(geometry, 14);
    coords.forEach(xyz -> {
      try {
        reader.read(xyz);
      } catch (TileStoreException ex) {
        ex.printStackTrace();
      }
    });
  }


}
