package io.gazetteer.cli.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name="osm", subcommands = {
    OSMImport.class,
    OSMUpdate.class,
})
public class OSM implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    CommandLine.usage(new OSM(), System.out);
    return 0;
  }

}
