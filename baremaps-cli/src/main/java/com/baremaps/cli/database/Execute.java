/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.cli.database;

import com.baremaps.cli.Options;
import com.baremaps.workflow.Workflow;
import com.baremaps.workflow.WorkflowExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "execute", description = "Execute a workflow.")
public class Execute implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Execute.class);

  @Mixin private Options options;

  @Option(
      names = {"--workflow"},
      paramLabel = "WORKFLOW",
      description = "The workflow file.",
      required = true)
  private Path config;

  @Override
  public Integer call() throws Exception {
    logger.info("Importing data");
    var mapper = new ObjectMapper();
    var workflow = mapper.readValue(config.toFile(), Workflow.class);
    new WorkflowExecutor(workflow).execute();
    logger.info("Done");
    return 0;
  }
}
