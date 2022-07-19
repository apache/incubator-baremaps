package com.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.testing.TestFiles;
import com.baremaps.workflow.PostgresBaseTest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportGeoJsonTest extends PostgresBaseTest {

  @Test
  void run() {
    var task = new ImportGeoJson("", List.of(), TestFiles.resolve("data.json").toString(), getJdbcUrl(), 4326, 3857);
    task.run();
  }
}