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

  @Option(names = {"--tileset"}, paramLabel = "TILESET", description = "The tileset file.",
      required = true)
  private Path tileset;

  @Option(names = {"--repository"}, paramLabel = "REPOSITORY", description = "The tile repository.",
      required = true)
  private Path repository;

  @Option(names = {"--tiles"}, paramLabel = "TILES", description = "The tiles to export.")
  private URI tiles;

  @Option(names = {"--format"}, paramLabel = "FORMAT",
      description = "The format of the repository.")
  private ExportVectorTiles.Format format = ExportVectorTiles.Format.file;

  @Override
  public Integer call() throws Exception {
    new ExportVectorTiles(tileset.toAbsolutePath(),
        repository.toAbsolutePath(), format)
            .execute(new WorkflowContext());
    return 0;
  }
}
