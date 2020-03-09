package com.baremaps.cli.commands;

import picocli.CommandLine.Option;

public class Mixins {

  @Option(names = { "--level" }, description = { "The log level." })
  protected String level = "INFO";

}
