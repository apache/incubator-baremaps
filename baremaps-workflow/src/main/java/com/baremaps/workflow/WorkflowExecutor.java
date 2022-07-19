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

package com.baremaps.workflow;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** A class for building and executing pipelines. */
public class WorkflowExecutor {

  private final Map<String, Step> steps;

  private final Map<String, CompletableFuture<Void>> futures;

  private final Graph<String> graph;

  public WorkflowExecutor(Workflow workflow) {
    this.steps = Arrays.stream(workflow.tasks()).collect(Collectors.toMap(s -> s.id(), s -> s));
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
      throw new WorkflowException("The pipeline has cycles in its execution graph");
    }
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
    Runnable step = () -> steps.get(id).task();
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
}
