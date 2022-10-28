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

package org.apache.baremaps.ogcapi;



import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;

@Singleton
@Path("")
public class ImportResource {

  private final Jdbi jdbi;

  @Inject
  public ImportResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("studio/import")
  public Response uploadData(@FormDataParam("file") InputStream fileInputStream,
      @FormDataParam("file") FormDataContentDisposition fileMetaData) {
    /*
     * TODO: replace GeoTools with Apache SIS // Read FeatureCollection FeatureJSON fjson = new
     * FeatureJSON(); var fc = fjson.readFeatureCollection(fileInputStream);
     * 
     * // Setup Collection String fileName = fileMetaData.getFileName(); Collection collection = new
     * Collection() .id(UUID.randomUUID()) .title(fileName.substring(0, fileName.lastIndexOf(".") -
     * 1)) .extent( new Extent() .spatial( new ExtentSpatial() .bbox( List.of( List.of(
     * fc.getBounds().getMinX(), fc.getBounds().getMinY(), fc.getBounds().getMaxX(),
     * fc.getBounds().getMaxY()))))) .count(fc.size()) .created(new Date()) .geometryType(
     * fc.getSchema().getGeometryDescriptor().getType().getBinding().getSimpleName());
     * 
     * // Load data jdbi.useTransaction( handle -> { // Create collection handle .createUpdate(
     * "insert into collections (id, collection) values (:id, CAST(:collection AS jsonb))")
     * .bind("id", collection.getId()) .bindByType("collection", collection, COLLECTION) .execute();
     * // Create table handle.execute( String.format(
     * "create table \"%s\" (id serial, tags hstore, geom geometry)", collection.getId())); //
     * Insert features var features = fc.features(); while (features.hasNext()) { SimpleFeature
     * feature = (SimpleFeature) features.next(); HashMap<String, String> properties = new
     * HashMap<>(); feature.getProperties().stream() .filter(property ->
     * !property.getName().getLocalPart().equals("geometry")) .forEach( property -> properties.put(
     * property.getName().getLocalPart(), property.getValue().toString())); Object geom =
     * feature.getDefaultGeometryProperty().getValue(); handle .createUpdate( String.format(
     * "insert into \"%s\" (tags, geom) values (:tags, ST_Transform(ST_SetSRID(CAST(:geom as geometry), 4326), 3857))"
     * , collection.getId())) .bind("tags", properties) .bind("geom", geom.toString()) .execute(); }
     * });
     * 
     * return Response.created(URI.create("collections/" + collection.getId())).build();
     */
    return Response.ok().build();
  }
}
