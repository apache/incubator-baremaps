/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.server;

import static com.google.common.net.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaders.Values.BINARY;

import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import de.javagl.jgltf.model.NodeModel;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.apache.baremaps.tdtiles.GltfBuilder;
import org.apache.baremaps.tdtiles.TdTilesStore;
import org.apache.baremaps.tdtiles.building.Building;
import org.apache.baremaps.tdtiles.subtree.Availability;
import org.apache.baremaps.tdtiles.subtree.Subtree;

public class TdTilesResources {

  private static final ResponseHeaders GLB_HEADERS = ResponseHeaders.builder(200)
      .add(CONTENT_TYPE, BINARY)
      .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .build();

  private static final ResponseHeaders JSON_HEADERS = ResponseHeaders.builder(200)
      .add(CONTENT_TYPE, APPLICATION_JSON)
      .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .build();

  private final TdTilesStore tdTilesStore;

  public TdTilesResources(DataSource dataSource) {
    this.tdTilesStore = new TdTilesStore(dataSource);
  }

  @Get("regex:^/subtrees/(?<level>[0-9]+).(?<x>[0-9]+).(?<y>[0-9]+).json")
  public HttpResponse getSubtree(@Param("level") int level, @Param("x") int x, @Param("y") int y) {
    if (level == 18) {
      return HttpResponse.ofJson(JSON_HEADERS,
          new Subtree(new Availability(false), new Availability(true), new Availability(false)));
    }
    return HttpResponse.ofJson(JSON_HEADERS,
        new Subtree(new Availability(true), new Availability(true), new Availability(true)));
  }

  @Get("regex:^/content/content_(?<level>[0-9]+)__(?<x>[0-9]+)_(?<y>[0-9]+).glb")
  public HttpResponse getContent(@Param("level") int level, @Param("x") int x, @Param("y") int y)
      throws Exception {
    if (level < 14) {
      return HttpResponse.of(GLB_HEADERS, HttpData.wrap(GltfBuilder.createGltf(new ArrayList<>())));
    }
    float[] coords = xyzToLatLonRadians(x, y, level);
    List<NodeModel> nodes = new ArrayList<>();
    int limit = level > 17 ? 1000 : level > 16 ? 200 : level > 15 ? 30 : 10;
    List<Building> buildings = tdTilesStore.read(coords[0], coords[1], coords[2], coords[3], limit);
    for (Building building : buildings) {
      float tolerance = level > 17 ? 0.00001f : level > 15 ? 0.00002f : 0.00004f;
      nodes.add(GltfBuilder.createNode(building, tolerance));
    }
    return HttpResponse.of(GLB_HEADERS, HttpData.wrap(GltfBuilder.createGltf(nodes)));
  }

  /**
   * Convert XYZ tile coordinates to lat/lon in radians.
   * 
   * @param x
   * @param y
   * @param z
   * @return
   */
  public static float[] xyzToLatLonRadians(int x, int y, int z) {
    float[] answer = new float[4];
    int subdivision = 1 << z;
    float yWidth = (float) Math.PI / subdivision;
    float xWidth = 2 * (float) Math.PI / subdivision;
    answer[0] = -(float) Math.PI / 2 + y * yWidth; // Lon
    answer[1] = answer[0] + yWidth; // Lon max
    answer[2] = -(float) Math.PI + xWidth * x; // Lat
    answer[3] = answer[2] + xWidth; // Lat max
    // Clamp to -PI/2 to PI/2
    answer[0] = Math.max(-(float) Math.PI / 2, Math.min((float) Math.PI / 2, answer[0]));
    answer[1] = Math.max(-(float) Math.PI / 2, Math.min((float) Math.PI / 2, answer[1]));
    // Clamp to -PI to PI
    answer[2] = Math.max(-(float) Math.PI, Math.min((float) Math.PI, answer[2]));
    answer[3] = Math.max(-(float) Math.PI, Math.min((float) Math.PI, answer[3]));
    return answer;
  }
}
