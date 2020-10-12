package com.baremaps;

import com.baremaps.osm.reader.ReaderException;
import com.baremaps.osm.reader.pbf.DataBlock;
import com.baremaps.osm.reader.pbf.FileBlockHandler;
import com.baremaps.osm.reader.pbf.FileBlockReader;
import com.baremaps.osm.reader.pbf.HeaderBlock;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
public class FileBlockReaderBenchmark {

  private Path path = Paths.get("./switzerland-latest.pbf");

  private FileBlockHandler handler = new FileBlockHandler() {

    @Override
    public void onHeaderBlock(HeaderBlock headerBlock) throws Exception {

    }

    @Override
    public void onDataBlock(DataBlock dataBlock) throws Exception {

    }
  };

  @Setup
  public void setup() throws IOException {
    URL url = new URL("http://download.geofabrik.de/europe/switzerland-latest.osm.pbf");
    if (!Files.exists(path)) {
      try (InputStream inputStream = url.openStream()) {
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 1)
  public void parallel() throws ReaderException {
    new FileBlockReader().read(path, handler);
  }

  public static void main(String[] args) throws IOException, ReaderException {
    FileBlockReaderBenchmark bench = new FileBlockReaderBenchmark();
    bench.setup();
    bench.parallel();
  }

}
