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

package org.apache.baremaps.tdtiles;

import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.*;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import de.javagl.jgltf.model.io.v2.GltfAssetsV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.apache.baremaps.tdtiles.building.Building;
import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.math.Vector3D;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class GltfBuilder {

    /**
     * Create a node from a building.
     *
     * @param building
     * @return
     */
    public static NodeModel createNode(Building building, float tolerance) {
        DelaunayTriangulationBuilder delaunayTriangulationBuilder = new DelaunayTriangulationBuilder();
        delaunayTriangulationBuilder.setSites(building.geometry());
        delaunayTriangulationBuilder.setTolerance(tolerance);

        float[] translation = cartesian3Degrees((float) building.geometry().getCoordinates()[0].y, (float) building.geometry().getCoordinates()[0].x, 0);

        Geometry triangulation = delaunayTriangulationBuilder.getTriangles(new GeometryFactory()); // initial triangulation
        if(triangulation.getNumGeometries() == 0){
            return new DefaultNodeModel();
        }

        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();

        createRoof(building, translation, triangulation, vertices, indices);
        HashSet<Edge> edges = getExteriorEdges(triangulation);
        createWalls(building, translation, vertices, indices, edges);
        createNormals(vertices, normals);

        MeshPrimitiveBuilder meshPrimitiveBuilder =
                MeshPrimitiveBuilder.create();
        meshPrimitiveBuilder.setIntIndicesAsShort(IntBuffer.wrap(ArrayUtils.toPrimitive(indices.toArray(new Integer[0]), 0)));
        meshPrimitiveBuilder.addPositions3D(FloatBuffer.wrap(ArrayUtils.toPrimitive(vertices.toArray(new Float[0]), 0.0F)));
        meshPrimitiveBuilder.addNormals3D(FloatBuffer.wrap(ArrayUtils.toPrimitive(normals.toArray(new Float[0]), 0.0F)));

        DefaultMeshPrimitiveModel meshPrimitiveModel =
                meshPrimitiveBuilder.build();

        // Create a material, and assign it to the mesh primitive
        MaterialBuilder materialBuilder = MaterialBuilder.create();
        materialBuilder.setBaseColorFactor(1f, 1f, 1f, 1.0f);
        materialBuilder.setDoubleSided(false);
        MaterialModelV2 materialModel = materialBuilder.build();
        materialModel.setMetallicFactor(0.0f);
        materialModel.setOcclusionStrength(0.0f);
        materialModel.setRoughnessFactor(1.0f);
        meshPrimitiveModel.setMaterialModel(materialModel);

        // Create a mesh with the mesh primitive
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.addMeshPrimitiveModel(meshPrimitiveModel);

        // Create a node with the mesh
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        nodeModel.addMeshModel(meshModel);
        //nodeModel.setScale(new float[]{5f,5f,5f});
        nodeModel.setTranslation(translation);
        return nodeModel;
    }

    /**
     * Compute the normal for each vertex in the vertices array.
     * Vertices are grouped by 3, so we can compute the normal for each triangle.
     * The normal is the normalized cross product of the two vectors of the triangle.
     *
     * @param vertices
     * @param normals
     */
    private static void createNormals(List<Float> vertices, List<Float> normals) {
        for (int i = 0; i < vertices.size(); i += 9) {
            float[] v1 = new float[]{vertices.get(i), vertices.get(i + 1), vertices.get(i + 2)};
            float[] v2 = new float[]{vertices.get(i + 3), vertices.get(i + 4), vertices.get(i + 5)};
            float[] v3 = new float[]{vertices.get(i + 6), vertices.get(i + 7), vertices.get(i + 8)};
            float[] u = new float[]{v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]};
            float[] v = new float[]{v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]};
            float[] normal = new float[]{u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0]};
            float length = (float) Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
            normal[0] /= length;
            normal[1] /= length;
            normal[2] /= length;
            normals.add(normal[0]);
            normals.add(normal[1]);
            normals.add(normal[2]);
            normals.add(normal[0]);
            normals.add(normal[1]);
            normals.add(normal[2]);
            normals.add(normal[0]);
            normals.add(normal[1]);
            normals.add(normal[2]);
        }
    }

    /**
     * Iterate over the edges and extrude them to create the walls based on the build height.
     *
     * @param building
     * @param translation
     * @param vertices
     * @param indices
     * @param edges the exterior edges
     */
    private static void createWalls(Building building, float[] translation, List<Float> vertices, List<Integer> indices, HashSet<Edge> edges) {
        for (Edge edge : edges) {
            Coordinate[] v = edge.getCoordinates();

            float[] pos0 = cartesian3Degrees((float)v[0].getY(), (float)v[0].getX(), 0);
            pos0[0] -= translation[0];
            pos0[1] -= translation[1];
            pos0[2] -= translation[2];
            float[] pos1 = cartesian3Degrees((float)v[1].getY(), (float)v[1].getX(), 0);
            pos1[0] -= translation[0];
            pos1[1] -= translation[1];
            pos1[2] -= translation[2];
            float[] pos2 = cartesian3Degrees((float)v[0].getY(), (float)v[0].getX(), building.height());
            pos2[0] -= translation[0];
            pos2[1] -= translation[1];
            pos2[2] -= translation[2];
            float[] pos3 = cartesian3Degrees((float)v[1].getY(), (float)v[1].getX(), building.height());
            pos3[0] -= translation[0];
            pos3[1] -= translation[1];
            pos3[2] -= translation[2];

            indices.add(vertices.size()/3);
            vertices.add(pos0[0]);
            vertices.add(pos0[1]);
            vertices.add(pos0[2]);
            indices.add(vertices.size()/3);
            vertices.add(pos1[0]);
            vertices.add(pos1[1]);
            vertices.add(pos1[2]);
            indices.add(vertices.size()/3);
            vertices.add(pos2[0]);
            vertices.add(pos2[1]);
            vertices.add(pos2[2]);

            indices.add(vertices.size()/3);
            vertices.add(pos2[0]);
            vertices.add(pos2[1]);
            vertices.add(pos2[2]);
            indices.add(vertices.size()/3);
            vertices.add(pos1[0]);
            vertices.add(pos1[1]);
            vertices.add(pos1[2]);
            indices.add(vertices.size()/3);
            vertices.add(pos3[0]);
            vertices.add(pos3[1]);
            vertices.add(pos3[2]);
        }
    }

    /**
     * Get a list of exterior edges from the building geometry. The exterior edges are the ones that are not shared by two
     * triangles.
     * @param triangulation
     * @return
     */
    private static HashSet<Edge> getExteriorEdges(Geometry triangulation) {
        HashSet<Edge> edges = new HashSet<>(triangulation.getNumGeometries() * 3, 1.0f);
        for (int i = 0; i < triangulation.getNumGeometries(); i++) {
            Geometry triangle = triangulation.getGeometryN(i);
            Coordinate corner1 = triangle.getCoordinates()[0];
            Coordinate corner2 = triangle.getCoordinates()[1];
            Coordinate corner3 = triangle.getCoordinates()[2];
            Edge edge1 = new Edge(new Coordinate[]{corner1, corner2});
            if(edges.contains(edge1)){
                edges.remove(edge1);
            } else {
                edges.add(edge1);
            }
            Edge edge2 = new Edge(new Coordinate[]{corner2, corner3});
            if(edges.contains(edge2)){
                edges.remove(edge2);
            } else {
                edges.add(edge2);
            }
            Edge edge3 = new Edge(new Coordinate[]{corner3, corner1});
            if(edges.contains(edge3)){
                edges.remove(edge3);
            } else {
                edges.add(edge3);
            }
        }
        return edges;
    }

    /**
     * Create the roof geometry. This is simply the triangulation of the building geometry at the building height.
     *
     * @param building
     * @param translation
     * @param triangulation
     * @param vertices
     * @param indices
     */
    private static void createRoof(Building building, float[] translation, Geometry triangulation, List<Float> vertices, List<Integer> indices) {
        for (int i = 0; i < triangulation.getNumGeometries(); i++) {
            Geometry triangle = triangulation.getGeometryN(i);
            Coordinate corner1 = triangle.getCoordinates()[0];
            Coordinate corner2 = triangle.getCoordinates()[1];
            Coordinate corner3 = triangle.getCoordinates()[2];

            float[] pos0 = cartesian3Degrees((float) corner1.y, (float) corner1.x, building.height());
            pos0[0] -= translation[0];
            pos0[1] -= translation[1];
            pos0[2] -= translation[2];
            float[] pos1 = cartesian3Degrees((float) corner2.y, (float) corner2.x, building.height());
            pos1[0] -= translation[0];
            pos1[1] -= translation[1];
            pos1[2] -= translation[2];
            float[] pos2 = cartesian3Degrees((float) corner3.y, (float) corner3.x, building.height());
            pos2[0] -= translation[0];
            pos2[1] -= translation[1];
            pos2[2] -= translation[2];

            indices.add(vertices.size() / 3);
            vertices.add(pos0[0]);
            vertices.add(pos0[1]);
            vertices.add(pos0[2]);
            indices.add(vertices.size() / 3);
            vertices.add(pos1[0]);
            vertices.add(pos1[1]);
            vertices.add(pos1[2]);
            indices.add(vertices.size() / 3);
            vertices.add(pos2[0]);
            vertices.add(pos2[1]);
            vertices.add(pos2[2]);
        }
    }

    /**
     * Create a GLB from a list of nodes.
     *
     * @param nodes
     * @return
     * @throws Exception
     */
    public static byte[] createGltf(List<NodeModel> nodes) throws Exception {
        DefaultSceneModel sceneModel = new DefaultSceneModel();

        for (NodeModel node : nodes) {
            sceneModel.addNode(node);
        }

        GltfModelBuilder gltfModelBuilder = GltfModelBuilder.create();
        gltfModelBuilder.addSceneModel(sceneModel);
        DefaultGltfModel gltfModel = gltfModelBuilder.build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GltfAssetV2 asset = GltfAssetsV2.createEmbedded(gltfModel);

        GltfAssetWriterV2 gltfAssetWriter = new GltfAssetWriterV2();
        gltfAssetWriter.writeBinary(asset, outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Returns a Cartesian3 position from longitude and latitude values given in radians.
     * Port of <a href="https://github.com/CesiumGS/cesium/blob/20c305e94d80a76ff750cfc2fea17d22cb7e5b58/packages/engine/Source/Core/Cartesian3.js#L914">CesiumGS</a>
     *
     * @param latitude in radians
     * @param longitude in radians
     * @param height in meters above the ellipsoid.
     * @return The position
     */
    public static float[] cartesian3FromRadians(float latitude, float longitude, float height) {
        Vector3D wgs84RadiiSquared = new Vector3D(6378137.0f * 6378137.0f, 6378137.0f * 6378137.0f, 6356752.3142451793f * 6356752.3142451793f);
        double cosLatitude = Math.cos(latitude);
        Vector3D scratchN = new Vector3D(cosLatitude * Math.cos(longitude), cosLatitude * Math.sin(longitude), Math.sin(latitude)).normalize();
        Vector3D scratchK = new Vector3D(wgs84RadiiSquared.getX() * scratchN.getX(), wgs84RadiiSquared.getY() * scratchN.getY(), wgs84RadiiSquared.getZ() * scratchN.getZ());
        double gamma = Math.sqrt(scratchN.dot(scratchK));
        scratchK = scratchK.divide(gamma);
        scratchN = new Vector3D(scratchN.getX() * height, scratchN.getY() * height, scratchN.getZ() * height);
        Vector3D result = scratchK.add(scratchN);
        return new float[]{(float) result.getX(), (float) result.getZ(), -(float) result.getY()};
    }

    /**
     * Returns a Cartesian3 position from longitude and latitude values given in degrees.
     * Port of <a href="https://github.com/CesiumGS/cesium/blob/20c305e94d80a76ff750cfc2fea17d22cb7e5b58/packages/engine/Source/Core/Cartesian3.js#L914">CesiumGS</a>
     *
     * @param latitude in degrees
     * @param longitude in degrees
     * @param height in meters above the ellipsoid.
     * @return The position
     */
    public static float[] cartesian3Degrees(float latitude, float longitude, float height) {
        return cartesian3FromRadians(latitude * (float) Math.PI / 180, longitude * (float) Math.PI / 180, height);
    }
}
