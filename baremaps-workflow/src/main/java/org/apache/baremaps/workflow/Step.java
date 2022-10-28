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

import java.util.List;

/**
 * A step is a group of tasks executed sequentially in a workflow.
 * A step may depend on other steps through the needs attribute.
 *
 * @param id the identifier of the step
 * @param needs the identifiers of the steps that must be executed before this step
 * @param tasks the tasks of the step
 */
public record Step(String id, List<String> needs, List<Task> tasks) {}
