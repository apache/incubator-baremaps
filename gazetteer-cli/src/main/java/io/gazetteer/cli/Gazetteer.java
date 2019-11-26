package io.gazetteer.cli;

import io.gazetteer.cli.commands.Import;
import io.gazetteer.cli.commands.Postgis;
import io.gazetteer.cli.commands.Serve;
import io.gazetteer.cli.commands.Tiles;
import io.gazetteer.cli.commands.Update;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(subcommands = {
    Import.class,
    Update.class,
    Tiles.class,
    Postgis.class,
    Serve.class,
})
public class Gazetteer implements Callable<Integer> {

  @Override
  public Integer call() {
    CommandLine.usage(new Gazetteer(), System.out);
    return 0;
  }

  public static void main(String[] args) {
    CommandLine cmd = new CommandLine(new Gazetteer());
    cmd.execute(args);
  }

}
