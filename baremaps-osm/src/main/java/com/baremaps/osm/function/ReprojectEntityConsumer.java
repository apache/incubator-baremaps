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

package com.baremaps.osm.function;

import com.baremaps.osm.domain.Element;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.geometry.ProjectionTransformer;
import org.locationtech.jts.geom.Geometry;

/** Changes the projection of the geometry of an entity via side-effects. */
public class ReprojectEntityConsumer implements EntityConsumerAdapter {

  private final ProjectionTransformer projectionTransformer;

  /**
   * Creates a consumer that reproject geometries with the provided SRIDs.
   *
   * @param inputSRID the input SRID
   * @param outputSRID the output SRID
   */
  public ReprojectEntityConsumer(int inputSRID, int outputSRID) {
    this.projectionTransformer = new ProjectionTransformer(inputSRID, outputSRID);
  }

  /** {@inheritDoc} */
  @Override
  public void match(Node node) {
    handleElement(node);
  }

  /** {@inheritDoc} */
  @Override
  public void match(Way way) {
    handleElement(way);
  }

  /** {@inheritDoc} */
  @Override
  public void match(Relation relation) {
    handleElement(relation);
  }

  /** {@inheritDoc} */
  private void handleElement(Element element) {
    if (element.getGeometry() != null) {
      Geometry geometry = projectionTransformer.transform(element.getGeometry());
      element.setGeometry(geometry);
    }
  }
}
