/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.pipeline;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.sis.setup.Configuration;

public class Pipeline {

  private final Context context;

  private final Map<String, Step> steps;

  private final Map<String, CompletableFuture<Void>> futures;

  private final Graph<String> graph;

  public Pipeline(Context context, List<Step> steps) {
    this.context = context;
    this.steps = steps.stream().collect(Collectors.toMap(s -> s.id(), s -> s));
    this.futures = new ConcurrentHashMap<>();

    // Build the execution graph
    ImmutableGraph.Builder<String> graphBuilder = GraphBuilder.directed().immutable();
    for (String id : this.steps.keySet()) {
      graphBuilder.addNode(id);
    }
    for (Step step : this.steps.values()) {
      for (String stepNeeded : step.needs()) {
        graphBuilder.putEdge(stepNeeded, step.id());
      }
    }
    this.graph = graphBuilder.build();
    if (Graphs.hasCycle(graph)) {
      throw new PipelineException("The pipeline has cycles in its execution graph");
    }

    // Use postgresql instead of derby to store crs data
    Configuration.current().setDatabase(() -> context.dataSource());
  }

  public CompletableFuture<Void> execute() {
    var endSteps =
        graph.nodes().stream()
            .filter(this::isEndStep)
            .map(this::getStep)
            .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(endSteps);
  }

  private boolean isEndStep(String id) {
    return graph.successors(id).isEmpty();
  }

  private CompletableFuture<Void> getStep(String id) {
    return futures.computeIfAbsent(id, this::computeStep);
  }

  private CompletableFuture<Void> computeStep(String id) {
    Runnable step = () -> steps.get(id).execute(context);
    var predecessors = graph.predecessors(id).stream().toList();
    if (predecessors.isEmpty()) {
      return CompletableFuture.runAsync(step);
    } else if (predecessors.size() == 1) {
      var previousStep = getStep(predecessors.stream().findFirst().get());
      return previousStep.thenRunAsync(step);
    } else {
      var previousSteps =
          CompletableFuture.allOf(
              predecessors.stream().map(this::getStep).toArray(CompletableFuture[]::new));
      return previousSteps.thenRunAsync(step);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Context context;

    private List<Step> steps = new ArrayList<>();

    public Builder setContext(Context context) {
      this.context = context;
      return this;
    }

    public Builder addStep(Step step) {
      this.steps.add(step);
      return this;
    }

    public Pipeline build() {
      return new Pipeline(context, steps);
    }
  }
}
