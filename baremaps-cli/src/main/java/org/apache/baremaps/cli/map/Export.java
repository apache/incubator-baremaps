/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.cli.map;



import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.ExportVectorTiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "export", description = "Export vector tiles from the database.")
public class Export implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Export.class);

  @Mixin
  private Options options;

  @Option(names = {"--database"}, paramLabel = "DATABASE",
      description = "The JDBC url of Postgres.", required = true)
  private String database;

  @Option(names = {"--tileset"}, paramLabel = "TILESET", description = "The tileset file.",
      required = true)
  private Path tileset;

  @Option(names = {"--repository"}, paramLabel = "REPOSITORY", description = "The tile repository.",
      required = true)
  private Path repository;

  @Option(names = {"--tiles"}, paramLabel = "TILES", description = "The tiles to export.")
  private URI tiles;

  @Option(names = {"--batch-array-size"}, paramLabel = "BATCH_ARRAY_SIZE",
      description = "The size of the batch array.")
  private int batchArraySize = 1;

  @Option(names = {"--batch-array-index"}, paramLabel = "READER",
      description = "The index of the batch in the array.")
  private int batchArrayIndex = 0;

  @Option(names = {"--mbtiles"}, paramLabel = "MBTILES",
      description = "The repository is in the MBTiles format.")
  private boolean mbtiles = false;

  @Override
  public Integer call() throws Exception {
    new ExportVectorTiles(database, tileset.toAbsolutePath().toString(),
        repository.toAbsolutePath().toString(), batchArraySize, batchArrayIndex, mbtiles)
            .execute(new WorkflowContext());
    return 0;
  }
}
