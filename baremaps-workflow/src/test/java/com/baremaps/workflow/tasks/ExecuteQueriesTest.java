package com.baremaps.workflow.tasks;

import com.baremaps.testing.TestFiles;
import com.baremaps.workflow.PostgresBaseTest;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ExecuteQueriesTest extends PostgresBaseTest {

  @Test
  @Tag("integration")
  void run() {
    var task = new ExecuteQueries("id", List.of(), getJdbcUrl(), TestFiles.resolve("queries.sql").toString());
    task.run();
  }
}