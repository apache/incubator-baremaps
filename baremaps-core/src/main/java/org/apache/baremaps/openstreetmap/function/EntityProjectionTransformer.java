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

package org.apache.baremaps.openstreetmap.function;



import java.util.function.Consumer;
import org.apache.baremaps.openstreetmap.model.*;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.locationtech.jts.geom.Geometry;

/** Changes the projection of the geometry of an entity via side-effects. */
public class EntityProjectionTransformer implements Consumer<Entity> {

  private final ProjectionTransformer projectionTransformer;

  /**
   * Creates a consumer that reproject geometries with the provided SRIDs.
   *
   * @param inputSRID the input SRID
   * @param outputSRID the output SRID
   */
  public EntityProjectionTransformer(int inputSRID, int outputSRID) {
    this.projectionTransformer = new ProjectionTransformer(inputSRID, outputSRID);
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Entity entity) {
    if (entity instanceof Element element && element.getGeometry() != null) {
      Geometry geometry = projectionTransformer.transform(element.getGeometry());
      element.setGeometry(geometry);
    }
  }
}
