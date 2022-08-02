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

package com.baremaps.cli.workflow;

import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;

import com.baremaps.cli.Options;
import com.baremaps.workflow.WorkflowExecutor;
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
      names = {"--file"},
      paramLabel = "FILE",
      description = "The workflow file.",
      required = true)
  private Path file;

  @Override
  public Integer call() throws Exception {
    logger.info("Executing the workflow {}", file);
    var mapper = defaultObjectMapper();
    var workflow = mapper.readValue(file.toFile(), com.baremaps.workflow.Workflow.class);
    try (var executor = new WorkflowExecutor(workflow)) {
      executor.execute().get();
    }
    logger.info("Finished executing the workflow {}", file);
    return 0;
  }
}
