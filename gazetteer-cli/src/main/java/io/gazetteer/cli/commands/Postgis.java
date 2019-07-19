package io.gazetteer.cli.commands;

import io.gazetteer.cli.commands.Postgis.Create;
import io.gazetteer.cli.commands.Postgis.Pull;
import io.gazetteer.cli.commands.Postgis.Remove;
import io.gazetteer.cli.commands.Postgis.Run;
import io.gazetteer.cli.commands.Postgis.Start;
import io.gazetteer.cli.commands.Postgis.Stop;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "postgis", subcommands = {
    Pull.class,
    Run.class,
    Create.class,
    Start.class,
    Stop.class,
    Remove.class,
})
public class Postgis implements Callable<Integer> {

  public static final String DOCKER_IMAGE = "gazetteerio/postgis:1";

  public static final String CONTAINER_NAME = "gazetteer-postgis";

  public static final String CONTAINER_PORT = "5432:5432";

  public static final String POSTGRES_DATABASE = "gazetteer";

  public static final String POSTGRES_USERNAME = "gazetteer";

  public static final String POSTGRES_PASSWORD = "gazetteer";

  @Command(name = "pull")
  public static class Pull implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      new ProcessBuilder("docker", "pull",
          DOCKER_IMAGE)
          .inheritIO()
          .start()
          .waitFor();
      return 0;
    }
  }

  @Command(name = "run")
  public static class Run implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      new ProcessBuilder("docker", "run",
          "--name", CONTAINER_NAME,
          "--publish", CONTAINER_PORT,
          "-e", String.format("POSTGRES_DB=%s", POSTGRES_DATABASE),
          "-e", String.format("POSTGRES_USER=%s", POSTGRES_USERNAME),
          "-e", String.format("POSTGRES_PASSWORD=%s", POSTGRES_PASSWORD),
          "-d",
          DOCKER_IMAGE)
          .inheritIO()
          .start()
          .waitFor();
      return 0;
    }
  }

  @Command(name = "create")
  public static class Create implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      new ProcessBuilder("docker", "create",
          "--name", CONTAINER_NAME,
          "--publish", CONTAINER_PORT,
          "-e", String.format("POSTGRES_DB=%s", POSTGRES_DATABASE),
          "-e", String.format("POSTGRES_USER=%s", POSTGRES_USERNAME),
          "-e", String.format("POSTGRES_PASSWORD=%s", POSTGRES_PASSWORD),
          DOCKER_IMAGE)
          .inheritIO()
          .start()
          .waitFor();
      return 0;
    }
  }

  @Command(name = "start")
  public static class Start implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      new ProcessBuilder("docker", "start", CONTAINER_NAME)
          .inheritIO()
          .start()
          .waitFor();
      return 0;
    }
  }

  @Command(name = "stop")
  public static class Stop implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      new ProcessBuilder("docker", "stop", CONTAINER_NAME)
          .inheritIO()
          .start()
          .waitFor();
      return 0;
    }
  }

  @Command(name = "remove")
  public static class Remove implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      new ProcessBuilder("docker", "rm", CONTAINER_NAME)
          .inheritIO()
          .start()
          .waitFor();
      return 0;
    }
  }

  @Override
  public Integer call() {
    CommandLine.usage(new Postgis(), System.out);
    return 0;
  }
}
