package org.apache.baremaps.benchmarking.geoparquet;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.geoparquet.GeoParquetReader;
import org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class OvertureMapsBenchmark {

    private static Path directory = Path.of("baremaps-benchmarking/data");

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OvertureMapsBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() throws IOException {
        if (!Files.exists(directory)) {
            try (var client = S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(new AnonymousAWSCredentialsProvider())
                    .build()) {

                var listRequest = ListObjectsV2Request.builder()
                        .bucket("overturemaps-us-west-2")
                        .prefix("release/2024-09-18.0/theme=addresses/")
                        .build();
                var objects = client.listObjectsV2(listRequest).contents();
                for (var object : objects) {
                    var key = object.key();
                    var name = key.substring(key.lastIndexOf("/") + 1);
                    var file = directory.resolve(name);
                    Files.createDirectories(file.getParent());
                    if (!Files.exists(file)) {
                        var getRequest = GetObjectRequest.builder()
                                .bucket("overturemaps-us-west-2")
                                .key(key)
                                .build();
                        client.getObject(getRequest, file);
                    }
                }
            }
        }
    }

    @Benchmark
    public void read() {
        GeoParquetReader reader = new GeoParquetReader(directory.toUri());
        reader.read().count();
    }

    @Benchmark
    public void readParallel() {
        GeoParquetReader reader = new GeoParquetReader(directory.toUri());
        reader.readParallel().count();
    }


}
