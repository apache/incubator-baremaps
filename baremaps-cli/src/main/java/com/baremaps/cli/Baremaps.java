package com.baremaps.cli;

import com.baremaps.cli.commands.Import;
import com.baremaps.cli.commands.Serve;
import com.baremaps.cli.commands.Export;
import com.baremaps.cli.commands.Update;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(subcommands = {
    Import.class,
    Update.class,
    Export.class,
    Serve.class,
}, name="baremaps")
public class Baremaps implements Callable<Integer> {

  @Override
  public Integer call() {
    CommandLine.usage(new Baremaps(), System.out);
    return 0;
  }

  public static void main(String[] args) {
    CommandLine cmd = new CommandLine(new Baremaps());
    cmd.execute(args);
  }

}
