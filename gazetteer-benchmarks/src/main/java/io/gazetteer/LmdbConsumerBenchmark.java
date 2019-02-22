package io.gazetteer;

import io.gazetteer.osm.lmdb.LmdbUtil;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.gazetteer.Constants.PBF_FILE;
import static io.gazetteer.Constants.TEMP_PREFIX;

public class LmdbConsumerBenchmark {

  public Path temp;

  public Stream<DataBlock> stream;

  public Consumer consumer;

  @Setup(Level.Invocation)
  public void prepare() throws IOException {
    temp = Files.createTempDirectory(TEMP_PREFIX);
    consumer = LmdbUtil.consumer(temp);
    InputStream input = getClass().getClassLoader().getResourceAsStream(PBF_FILE);
    stream = PBFUtil.dataBlocks(input);
  }

  @TearDown(Level.Invocation)
  public void check() throws IOException {
    if (Files.exists(temp)) Files.walk(temp).map(Path::toFile).forEach(File::delete);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public void processStream() throws ExecutionException, InterruptedException {
    ForkJoinPool executor = new ForkJoinPool(1);
    executor.submit(() -> stream.forEach(consumer)).get();
  }
}
