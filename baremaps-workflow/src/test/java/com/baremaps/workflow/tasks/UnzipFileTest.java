package com.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.collection.utils.FileUtils;
import com.baremaps.testing.TestFiles;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class UnzipFileTest {

  @Test
  void run() throws IOException {
    var zip = TestFiles.resolve("monaco-shapefile.zip");
    var directory = Files.createTempDirectory("tmp_");
    var task = new UnzipFile("id", List.of(), zip.toString(), directory.toString());
    task.run();
    FileUtils.deleteRecursively(directory);
  }
}