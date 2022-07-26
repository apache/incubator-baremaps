package com.baremaps.cli.workflow;

import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;

import com.baremaps.workflow.Step;
import com.baremaps.workflow.Workflow;
import com.baremaps.workflow.tasks.LogMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Initialize a workflow.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Option(
      names = {"--file"},
      paramLabel = "FILE",
      description = "A workflow file.",
      required = true)
  private Path workflow;

  @Override
  public Integer call() throws Exception {
    com.baremaps.workflow.Workflow workflowObject = new Workflow(
        List.of(new Step("hello", List.of(), List.of(new LogMessage("Hello World!")))));
    Files.write(
        workflow, defaultObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsBytes(workflowObject));
    logger.info("Workflow initialized");
    return 0;
  }
}
