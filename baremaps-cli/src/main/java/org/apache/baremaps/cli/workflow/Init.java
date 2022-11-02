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

package org.apache.baremaps.cli.workflow;

import static org.apache.baremaps.server.DefaultObjectMapper.defaultObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.baremaps.workflow.Step;
import org.apache.baremaps.workflow.Workflow;
import org.apache.baremaps.workflow.tasks.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Initialize a workflow.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Option(names = {"--file"}, paramLabel = "FILE", description = "A workflow file.",
      required = true)
  private Path workflow;

  @Override
  public Integer call() throws Exception {
    org.apache.baremaps.workflow.Workflow workflowObject = new Workflow(
        List.of(new Step("hello", List.of(), List.of(new LogMessage("Hello World!")))));
    Files.write(workflow,
        defaultObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(workflowObject));
    logger.info("Workflow initialized");
    return 0;
  }
}
