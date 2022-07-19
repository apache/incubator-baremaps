package com.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.collection.utils.FileUtils;
import com.baremaps.testing.TestFiles;
import com.baremaps.workflow.PostgresBaseTest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportShapefileTest extends PostgresBaseTest {

  @Test
  void run() throws IOException {
    var zip = TestFiles.resolve("monaco-shapefile.zip");
    var directory = Files.createTempDirectory("tmp_");
    var unzip = new UnzipFile("id", List.of(), zip.toString(), directory.toString());
    unzip.run();
    var task = new ImportShapefile("id", List.of(), directory.resolve("gis_osm_buildings_a_free_1.shp").toString(), getJdbcUrl(), 4326, 3857);
    task.run();
    FileUtils.deleteRecursively(directory);
  }
}