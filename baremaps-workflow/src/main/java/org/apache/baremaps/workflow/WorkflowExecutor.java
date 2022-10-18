/*
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

package org.apache.baremaps.workflow;



import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** A class for building and executing pipelines. */
public class WorkflowExecutor implements AutoCloseable {

  private final ExecutorService executorService;

  private final Map<String, Step> steps;

  private final Map<String, CompletableFuture<Void>> futures;

  private final Graph<String> graph;

  public WorkflowExecutor(Workflow workflow) {
    this(workflow, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }

  public WorkflowExecutor(Workflow workflow, ExecutorService executorService) {
    this.executorService = executorService;
    this.steps = workflow.steps().stream().collect(Collectors.toMap(s -> s.id(), s -> s));
    this.futures = new ConcurrentHashMap<>();

    // Build the execution graph
    ImmutableGraph.Builder<String> graphBuilder = GraphBuilder.directed().immutable();
    for (String id : this.steps.keySet()) {
      graphBuilder.addNode(id);
    }
    for (Step step : this.steps.values()) {
      if (step.needs() != null) {
        for (String stepNeeded : step.needs()) {
          graphBuilder.putEdge(stepNeeded, step.id());
        }
      }
    }
    this.graph = graphBuilder.build();
    if (Graphs.hasCycle(graph)) {
      throw new WorkflowException("The workflow must be a directed acyclic graph");
    }
  }

  public CompletableFuture<Void> execute() {
    var endSteps = graph.nodes().stream().filter(this::isEndStep).map(this::getStep)
        .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(endSteps);
  }

  private CompletableFuture<Void> getStep(String step) {
    return futures.computeIfAbsent(step, this::initStep);
  }

  private CompletableFuture<Void> initStep(String step) {
    var future = previousSteps(step);
    for (Task task : steps.get(step).tasks()) {
      future = future.thenRunAsync(task, executorService);
    }
    return future;
  }

  private CompletableFuture<Void> previousSteps(String step) {
    var predecessors = graph.predecessors(step).stream().toList();
    if (predecessors.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    } else if (predecessors.size() == 1) {
      return getStep(predecessors.get(0));
    } else {
      return CompletableFuture
          .allOf(predecessors.stream().map(this::getStep).toArray(CompletableFuture[]::new));
    }
  }

  private boolean isEndStep(String step) {
    return graph.successors(step).isEmpty();
  }

  @Override
  public void close() throws Exception {
    executorService.shutdown();
  }
}
