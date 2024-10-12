package org.apache.baremaps.benchmarking.geoparquet;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.geoparquet.GeoParquetReader;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class SmallFileBenchmark {

    private Path source = Path.of("baremaps-testing/data/samples/example.parquet").toAbsolutePath();
    private Path directory = Path.of("baremaps-benchmarking/small").toAbsolutePath();

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SmallFileBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws IOException {
        if (!Files.exists(directory)) {
            for (int i = 0; i < 1000; i++) {
                Path target = directory.resolve(i + ".parquet");
                Files.createDirectories(target.getParent());
                Files.copy(source, target);
            }
        }
    }

    @Benchmark
    public void read() {
        GeoParquetReader reader = new GeoParquetReader(Path.of("baremaps-benchmarking/small/*.parquet").toUri());
        reader.read().count();
    }

    @Benchmark
    public void readParallel() {
        GeoParquetReader reader = new GeoParquetReader(Path.of("baremaps-benchmarking/small/*.parquet").toUri());
        reader.readParallel().count();
    }
}