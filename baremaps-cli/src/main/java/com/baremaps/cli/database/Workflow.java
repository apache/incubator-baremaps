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
import com.baremaps.workflow.WorkflowExecutor;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "workflow", description = "Execute a workflow.")
public class Workflow implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Workflow.class);

  @Mixin private Options options;

  @Option(
      names = {"--workflow"},
      paramLabel = "WORKFLOW",
      description = "The workflow file.",
      required = true)
  private Path workflow;

  @Override
  public void run() {
    try {
      logger.info("Importing data");
      var mapper = new ObjectMapper();
      var workflow = mapper.readValue(this.workflow.toFile(), com.baremaps.workflow.Workflow.class);
      new WorkflowExecutor(workflow).execute().get();
      logger.info("Done");
    } catch (StreamReadException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      logger.error("Unable to read workflow", e);
      throw new RuntimeException(e);
    } catch (Exception e) {
      logger.error("Unable to execute workflow", e);
      throw new RuntimeException(e);
    }
  }
}
