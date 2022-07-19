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

package com.baremaps.workflow.tasks;

import com.baremaps.storage.geopackage.GeoPackageStore;
import com.baremaps.workflow.WorkflowException;
import java.nio.file.Paths;
import java.util.List;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;

public record ImportGeoPackage(
    String id,
    List<String> needs,
    String file,
    String database,
    Integer sourceSRID,
    Integer targetSRID)
    implements ImportFeatureTask {

  @Override
  public void run() {
    try {
      var path = Paths.get(file);
      var geoPackageStore = new GeoPackageStore(GeoPackageManager.open(path.toFile()));
      for (Resource resource : geoPackageStore.components()) {
        if (resource instanceof FeatureSet featureSet) {
          saveFeatureSet(featureSet);
        }
      }
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
