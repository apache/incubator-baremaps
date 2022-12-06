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
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * A workflow executor executes a workflow in parallel.
 */
public class WorkflowExecutor implements AutoCloseable {

  private final ExecutorService executorService;

  private final WorkflowContext context;

  private final Map<String, Step> steps;

  private final Map<String, CompletableFuture<Void>> futures;

  private final Graph<String> graph;

  /**
   * Constructs a workflow executor.
   *
   * @param workflow the workflow to execute
   */
  public WorkflowExecutor(Workflow workflow) {
    this(workflow, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }

  /**
   * Constructs a workflow executor.
   *
   * @param workflow the workflow to execute
   * @param executorService the executor service used to execute the tasks
   */
  public WorkflowExecutor(Workflow workflow, ExecutorService executorService) {
    this.executorService = executorService;
    this.context = new WorkflowContext();
    this.steps = workflow.getSteps().stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
    this.futures = new ConcurrentSkipListMap<>();

    // Build the execution graph
    ImmutableGraph.Builder<String> graphBuilder = GraphBuilder.directed().immutable();
    for (String id : this.steps.keySet()) {
      graphBuilder.addNode(id);
    }
    for (Step step : this.steps.values()) {
      if (step.getNeeds() != null) {
        for (String stepNeeded : step.getNeeds()) {
          graphBuilder.putEdge(stepNeeded, step.getId());
        }
      }
    }
    this.graph = graphBuilder.build();
    if (Graphs.hasCycle(this.graph)) {
      throw new WorkflowException("The workflow must be a directed acyclic graph");
    }
  }

  /**
   * Executes the workflow.
   */
  public CompletableFuture<Void> execute() {
    var endSteps = graph.nodes().stream().filter(this::isEndStep).map(this::getStep)
        .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(endSteps);
  }

  private CompletableFuture<Void> getStep(String step) {
    return futures.computeIfAbsent(step, this::initStep);
  }

  private CompletableFuture<Void> initStep(String stepId) {
    var future = previousSteps(stepId);
    for (Task task : steps.get(stepId).getTasks()) {
      future = future.thenRunAsync(() -> {
        try {
          task.execute(context);
        } catch (Exception e) {
          throw new WorkflowException(e);
        }
      }, executorService);
    }
    return future;
  }

  private CompletableFuture<Void> previousSteps(String stepId) {
    var predecessors = graph.predecessors(stepId).stream().toList();
    if (predecessors.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    } else if (predecessors.size() == 1) {
      return getStep(predecessors.get(0));
    } else {
      return CompletableFuture
          .allOf(predecessors.stream().map(this::getStep).toArray(CompletableFuture[]::new));
    }
  }

  private boolean isEndStep(String stepId) {
    return graph.successors(stepId).isEmpty();
  }

  @Override
  public void close() throws Exception {
    executorService.shutdown();
  }
}
