package com.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.testing.TestFiles;
import com.baremaps.workflow.PostgresBaseTest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportOsmPbfTest extends PostgresBaseTest {

  @Test
  void run() {
    var task = new ImportOsmPbf("id", List.of(), TestFiles.resolve("data.osm.pbf").toString(), getJdbcUrl(), 4326, 3857);
    task.run();
  }

}