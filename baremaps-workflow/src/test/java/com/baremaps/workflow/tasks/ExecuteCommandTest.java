package com.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ExecuteCommandTest {

  @Test
  void run() throws IOException {
    var path = Paths.get("test.txt").toAbsolutePath();
    new ExecuteCommand(String.format("echo test > %s", path)).run();
    assertTrue(Files.exists(path));
    assertTrue(Files.readString(path).contains("test"));
    Files.deleteIfExists(path);

  }
}