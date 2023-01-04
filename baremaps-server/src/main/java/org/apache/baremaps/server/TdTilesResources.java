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

package org.apache.baremaps.server;

import static com.google.common.net.HttpHeaders.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import de.javagl.jgltf.model.NodeModel;
import org.apache.baremaps.tdtiles.TdTilesStore;
import org.apache.baremaps.tdtiles.building.Building;
import org.apache.baremaps.tdtiles.GltfBuilder;
import org.apache.baremaps.tdtiles.subtree.Availability;
import org.apache.baremaps.tdtiles.subtree.Subtree;

@Singleton
@javax.ws.rs.Path("/")
public class TdTilesResources {
  private final TdTilesStore tdTilesStore;

  @Inject
  public TdTilesResources(DataSource dataSource) {
    this.tdTilesStore = new TdTilesStore(dataSource);
  }

  @GET
  @javax.ws.rs.Path("/subtrees/{level}.{x}.{y}.json")
  public Response getSubtree(@PathParam("level") int level, @PathParam("x") int x,
      @PathParam("y") int y) {
    if (level == 18) {
        return Response.ok().entity(new Subtree(new Availability(false),new Availability(true),new Availability(false))).header(CONTENT_TYPE, "application/json").build();
    }
    return Response.ok().entity(new Subtree(new Availability(true),new Availability(true),new Availability(true))).header(CONTENT_TYPE, "application/json").build();
  }

  @GET
  @javax.ws.rs.Path("/content/content_{level}__{x}_{y}.glb")
  public Response getContent(@PathParam("level") int level, @PathParam("x") int x,
      @PathParam("y") int y) throws Exception {
    float[] coords = xyzToLatLonRadians(x, y, level);
    int limit = level > 17 ? 1000 : 80;
    List<Building> buildings = tdTilesStore.read(coords[0],coords[1],coords[2],coords[3], limit);
    List<NodeModel> nodes = new ArrayList<>();
    for (Building building : buildings) {
      float tolerance = level > 17 ? 0.00001f : level >= 16 ? 0.0001f : 0.0003f;
      nodes.add(GltfBuilder.createNode(building, tolerance));
    }
    return Response.ok().entity(
            GltfBuilder.createGltf(nodes)
    ).build();
  }

  public static float[] xyzToLatLonRadians(int x, int y, int z) {
    float[] answer = new float[4];
    int  subdivision = 1 << z;
    float yWidth = (float)Math.PI / subdivision;
    float xWidth = 2 * (float)Math.PI / subdivision;
    answer[0] = - (float)Math.PI / 2 + y * yWidth; // Lon
    answer[1] = answer[0] + yWidth; // Lon max
    answer[2] = - (float)Math.PI + xWidth * x; // Lat
    answer[3] = answer[2] + xWidth; // Lat max
    // Clamp to -PI/2 to PI/2
    answer[0] = Math.max(- (float)Math.PI / 2, Math.min((float)Math.PI / 2, answer[0]));
    answer[1] = Math.max(- (float)Math.PI / 2, Math.min((float)Math.PI / 2, answer[1]));
    // Clamp to -PI to PI
    answer[2] = Math.max(- (float)Math.PI, Math.min((float)Math.PI, answer[2]));
    answer[3] = Math.max(- (float)Math.PI, Math.min((float)Math.PI, answer[3]));
    return answer;
  }

  @GET
  @javax.ws.rs.Path("/{path:.*}")
  public Response get(@PathParam("path") String path) {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("tdtiles/%s", path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (IOException e) {
      return Response.status(404).build();
    }
  }
}
