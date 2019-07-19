package io.gazetteer.cli;

import io.gazetteer.cli.commands.OSM;
import io.gazetteer.cli.commands.Postgis;
import io.gazetteer.cli.commands.Tiles;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(subcommands = {
    OSM.class,
    Tiles.class,
    Postgis.class,
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
