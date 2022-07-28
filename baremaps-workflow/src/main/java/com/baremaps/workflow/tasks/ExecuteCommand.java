package com.baremaps.workflow.tasks;

import com.baremaps.workflow.Task;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ExecuteCommand(String command) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ExecuteCommand.class);

  @Override
  public void run() {
    try {
      new ProcessBuilder().command("/bin/sh", "-c", command).start().waitFor();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
