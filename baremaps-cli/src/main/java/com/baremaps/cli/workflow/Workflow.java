package com.baremaps.cli.workflow;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "workflow",
    subcommands = {
        Init.class,
        Execute.class
    },
    description = "Manage a workflow.")
public class Workflow implements Runnable {

  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }
}
