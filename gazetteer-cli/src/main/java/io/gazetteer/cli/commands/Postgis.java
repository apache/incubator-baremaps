package io.gazetteer.cli.commands;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "postgis")
public class Postgis implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    DefaultDockerClientConfig config = DefaultDockerClientConfig
        .createDefaultConfigBuilder()
        .withRegistryUrl("https://hub.docker.com/r/")
        .build();

    DockerClient docker = DockerClientBuilder.getInstance(config).build();

    docker.pullImageCmd("gazetteerio/postgis:1")
        .exec(new PullImageResultCallback())
        .awaitCompletion();

    Optional<Container> container = docker.listContainersCmd()
        .exec()
        .stream()
        .filter(c -> Arrays.asList(c.getNames()).contains("/gazetteer-postgis"))
        .findFirst();

    if (!container.isPresent()) {
      docker.createContainerCmd("gazetteerio/postgis:1")
          .withName("gazetteer-postgis")
          .withPortBindings(PortBinding.parse("5432:5432"))
          .exec();
    }

    docker.stopContainerCmd("gazetteer-postgis").exec();
    docker.startContainerCmd("gazetteer-postgis").exec();

    return 0;
  }
}
