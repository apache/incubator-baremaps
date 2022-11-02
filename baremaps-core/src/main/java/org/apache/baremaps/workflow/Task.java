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



import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.baremaps.workflow.tasks.*;

/**
 * A task is a unit of work executed in a step of a workflow.
 */
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = DownloadUrl.class, name = "DownloadUrl"),
    @JsonSubTypes.Type(value = ExecuteCommand.class, name = "ExecuteCommand"),
    @JsonSubTypes.Type(value = ExecuteSql.class, name = "ExecuteSql"),
    @JsonSubTypes.Type(value = ExportVectorTiles.class, name = "ExportVectorTiles"),
    @JsonSubTypes.Type(value = ImportGeoPackage.class, name = "ImportGeoPackage"),
    @JsonSubTypes.Type(value = ImportOpenStreetMap.class, name = "ImportOpenStreetMap"),
    @JsonSubTypes.Type(value = ImportShapefile.class, name = "ImportShapefile"),
    @JsonSubTypes.Type(value = LogMessage.class, name = "LogMessage"),
    @JsonSubTypes.Type(value = UnzipFile.class, name = "UnzipFile"),
    @JsonSubTypes.Type(value = UpdateOpenStreetMap.class, name = "UpdateOpenStreetMap"),})
public interface Task {

  /**
   * Executes the task.
   *
   * @param context the context of the workflow
   * @throws Exception if an error occurs during the execution of the task
   */
  void execute(WorkflowContext context) throws Exception;

}
