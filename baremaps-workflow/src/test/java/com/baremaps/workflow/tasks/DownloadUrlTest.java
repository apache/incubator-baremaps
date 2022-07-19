package com.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class DownloadUrlTest {

  @Test
  @Tag("integration")
  void run() throws IOException {
    var file = File.createTempFile("test", ".tmp");
    file.deleteOnExit();
    var task = new DownloadUrl("id", List.of(), "https://raw.githubusercontent.com/baremaps/baremaps/main/README.md", file.getAbsolutePath());
    task.run();
    assertTrue(Files.readString(file.toPath()).contains("Baremaps"));
  }
}