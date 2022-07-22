package com.baremaps.workflow.tasks;

import com.baremaps.workflow.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LogMessage(String message) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(LogMessage.class);

  @Override
  public void run() {
    logger.info(message);
  }
}
