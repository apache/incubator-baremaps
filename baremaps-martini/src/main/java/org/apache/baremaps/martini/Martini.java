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

package org.apache.baremaps.martini;

/**
 * The {@code Martini} class is a port of the MARTINI algorithm for generating 3D terrain meshes
 * from height data.
 * <p>
 * 
 * @see <a href="https://github.com/mapbox/martini">Martini GitHub</a>
 */
public class Martini {

  private final int gridSize;
  private final int numTriangles;
  private final int numParentTriangles;
  private final int[] baseCoords;

  /**
   * Constructs a new {@code Martini} instance with the specified grid size.
   *
   * @param gridSize the grid size
   */
  public Martini(int gridSize) {
    this.gridSize = gridSize;

    int tileSize = gridSize - 1;
    if ((tileSize & (tileSize - 1)) != 0) {
      throw new IllegalArgumentException("Expected grid size to be 2^n+1, got " + gridSize + ".");
    }

    this.numTriangles = tileSize * tileSize * 2 - 2;
    this.numParentTriangles = this.numTriangles - tileSize * tileSize;

    this.baseCoords = new int[this.numTriangles * 4];
    for (int i = 0; i < this.numTriangles; i++) {
      int id = i + 2;
      int ax = 0, ay = 0, bx = 0, by = 0, cx = 0, cy = 0;
      if ((id & 1) != 0) {
        bx = by = cx = tileSize;
      } else {
        ax = ay = cy = tileSize;
      }
      while ((id >>= 1) > 1) {
        int mx = (ax + bx) >> 1;
        int my = (ay + by) >> 1;

        if ((id & 1) != 0) {
          bx = ax;
          by = ay;
          ax = cx;
          ay = cy;
        } else {
          ax = bx;
          ay = by;
          bx = cx;
          by = cy;
        }
        cx = mx;
        cy = my;
      }
      int k = i * 4;
      this.baseCoords[k] = ax;
      this.baseCoords[k + 1] = ay;
      this.baseCoords[k + 2] = bx;
      this.baseCoords[k + 3] = by;
    }
  }

  /**
   * Creates a new {@code Tile} instance with the specified terrain data.
   *
   * @param terrain the terrain data
   * @return the tile
   */
  public Tile createTile(float[] terrain) {
    return new Tile(terrain, gridSize, numTriangles, numParentTriangles, baseCoords);
  }

  /**
   * The {@code Tile} class represents a tile of terrain data.
   */
  public static class Tile {

    private final int gridSize;
    private final int[] indices;
    private final float[] errors;

    private int numVertices;
    private int numTriangles;

    private int[] vertices;
    private int[] triangles;
    private int triIndex = 0;

    private Tile(float[] terrain, int gridSize, int numTriangles, int numParentTriangles,
        int[] coords) {
      if (terrain.length != gridSize * gridSize) {
        throw new IllegalArgumentException(
            "Expected terrain data of length " + (gridSize * gridSize) + " (" + gridSize + " x "
                + gridSize + "), got " + terrain.length + ".");
      }

      this.gridSize = gridSize;
      this.indices = new int[this.gridSize * this.gridSize];

      this.errors = new float[terrain.length];
      for (int i = numTriangles - 1; i >= 0; i--) {
        int k = i * 4;
        int ax = coords[k];
        int ay = coords[k + 1];
        int bx = coords[k + 2];
        int by = coords[k + 3];
        int mx = (ax + bx) >> 1;
        int my = (ay + by) >> 1;
        int cx = mx + my - ay;
        int cy = my + ax - mx;

        float interpolatedHeight = (terrain[ay * gridSize + ax] + terrain[by * gridSize + bx]) / 2;
        int middleIndex = my * gridSize + mx;
        float middleError = Math.abs(interpolatedHeight - terrain[middleIndex]);

        errors[middleIndex] = Math.max(errors[middleIndex], middleError);

        if (i < numParentTriangles) {
          int leftChildIndex = ((ay + cy) >> 1) * gridSize + ((ax + cx) >> 1);
          int rightChildIndex = ((by + cy) >> 1) * gridSize + ((bx + cx) >> 1);
          errors[middleIndex] = Math.max(errors[middleIndex],
              Math.max(errors[leftChildIndex], errors[rightChildIndex]));
        }
      }
    }

    /**
     * Returns the mesh of the tile with the specified maximum error.
     *
     * @param maxError the maximum error
     * @return the mesh
     */
    public Mesh getMesh(float maxError) {
      int max = gridSize - 1;

      numVertices = 0;
      numTriangles = 0;
      countElements(0, 0, max, max, max, 0, maxError);
      countElements(max, max, 0, 0, 0, max, maxError);

      vertices = new int[numVertices * 2];
      triangles = new int[numTriangles * 3];
      triIndex = 0;
      processTriangle(0, 0, max, max, max, 0, maxError);
      processTriangle(max, max, 0, 0, 0, max, maxError);

      return new Mesh(vertices, triangles);
    }

    private void countElements(int ax, int ay, int bx, int by, int cx, int cy, float maxError) {
      int mx = (ax + bx) >> 1;
      int my = (ay + by) >> 1;

      if (Math.abs(ax - cx) + Math.abs(ay - cy) > 1 && errors[my * gridSize + mx] > maxError) {
        countElements(cx, cy, ax, ay, mx, my, maxError);
        countElements(bx, by, cx, cy, mx, my, maxError);
      } else {
        indices[ay * gridSize + ax] =
            indices[ay * gridSize + ax] != 0 ? indices[ay * gridSize + ax] : ++numVertices;
        indices[by * gridSize + bx] =
            indices[by * gridSize + bx] != 0 ? indices[by * gridSize + bx] : ++numVertices;
        indices[cy * gridSize + cx] =
            indices[cy * gridSize + cx] != 0 ? indices[cy * gridSize + cx] : ++numVertices;
        numTriangles++;
      }
    }

    private void processTriangle(int ax, int ay, int bx, int by, int cx, int cy, float maxError) {
      int mx = (ax + bx) >> 1;
      int my = (ay + by) >> 1;

      if (Math.abs(ax - cx) + Math.abs(ay - cy) > 1 && errors[my * gridSize + mx] > maxError) {
        processTriangle(cx, cy, ax, ay, mx, my, maxError);
        processTriangle(bx, by, cx, cy, mx, my, maxError);
      } else {
        int a = indices[ay * gridSize + ax] - 1;
        int b = indices[by * gridSize + bx] - 1;
        int c = indices[cy * gridSize + cx] - 1;

        vertices[2 * a] = ax;
        vertices[2 * a + 1] = ay;
        vertices[2 * b] = bx;
        vertices[2 * b + 1] = by;
        vertices[2 * c] = cx;
        vertices[2 * c + 1] = cy;

        triangles[triIndex++] = a;
        triangles[triIndex++] = b;
        triangles[triIndex++] = c;
      }
    }
  }

  /**
   * The {@code Mesh} class represents a mesh of vertices and triangles.
   */
  public static class Mesh {

    private final int[] vertices;
    private final int[] triangles;

    private Mesh(int[] vertices, int[] triangles) {
      this.vertices = vertices;
      this.triangles = triangles;
    }

    public int[] getVertices() {
      return vertices;
    }

    public int[] getTriangles() {
      return triangles;
    }
  }
}
