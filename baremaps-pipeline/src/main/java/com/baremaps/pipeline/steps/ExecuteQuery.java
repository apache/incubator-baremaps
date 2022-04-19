package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.Step;
import java.util.List;

public record ExecuteQuery(String id, List<String> needs, String file) implements Step {

  @Override
  public void execute(Context context) {

  }
}
