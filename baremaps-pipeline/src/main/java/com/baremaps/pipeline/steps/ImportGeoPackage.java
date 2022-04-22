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

package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.storage.geopackage.GeoPackageStore;
import java.util.List;
import mil.nga.geopackage.GeoPackageManager;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Resource;

public record ImportGeoPackage(
    String id, List<String> needs, String file, Integer sourceSRID, Integer targetSRID)
    implements ImportFeatureStep {

  @Override
  public void execute(Context context) {
    try {
      var path = context.directory().resolve(file);
      var geoPackageStore = new GeoPackageStore(GeoPackageManager.open(path.toFile()));
      for (Resource resource : geoPackageStore.components()) {
        if (resource instanceof FeatureSet featureSet) {
          saveFeatureSet(context, featureSet);
        }
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
