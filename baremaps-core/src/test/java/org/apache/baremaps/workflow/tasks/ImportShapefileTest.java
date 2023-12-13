/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;



import java.nio.file.Files;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.DecompressFile.Compression;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ImportShapefileTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void execute() throws Exception {
    var source = TestFiles.resolve("monaco-shapefile.zip");
    var target = Files.createTempDirectory("tmp_");
    var decompressFile = new DecompressFile(source, target, Compression.zip);
    decompressFile.execute(new WorkflowContext());
    var importShapefile = new ImportShapefile(target.resolve("gis_osm_buildings_a_free_1.shp"),
        4326, jdbcUrl(), 3857);
    importShapefile.execute(new WorkflowContext());
    FileUtils.deleteRecursively(target);
  }
}
