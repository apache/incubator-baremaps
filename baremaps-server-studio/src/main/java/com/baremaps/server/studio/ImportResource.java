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

package com.baremaps.server.studio;

import com.baremaps.model.Collection;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("")
public class ImportResource {

  private static final Logger logger = LoggerFactory.getLogger(ImportResource.class);

  private static final QualifiedType<ObjectNode> ENTITY =
      QualifiedType.of(ObjectNode.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public ImportResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("studio/import")
  public Response uploadData(
      @FormDataParam("file") InputStream fileInputStream,
      @FormDataParam("file") FormDataContentDisposition fileMetaData)
      throws Exception {
    // Setup Collection
    String fileName = fileMetaData.getFileName();
    Collection collection =
        new Collection()
            .id(UUID.randomUUID())
            .title(fileName.substring(0, fileName.lastIndexOf(".") - 1));

    // Read FeatureCollection
    FeatureJSON fjson = new FeatureJSON();
    FeatureCollection fc = fjson.readFeatureCollection(fileInputStream);
    SimpleFeatureType schema = (SimpleFeatureType) fc.getSchema();

    // Build FeatureType
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(collection.getId().toString());
    builder.setCRS(schema.getCoordinateReferenceSystem()); // <- Coordinate reference system
    builder.addAll(schema.getAttributeDescriptors());
    SimpleFeatureType SCHEMA = builder.buildFeatureType();

    // Setup DataStore
    Map<String, Object> params = new HashMap<>();
    Properties properties = jdbi.withHandle(handle -> handle.getConnection().getClientInfo());
    params.put("dbtype", "postgis");
    params.put("host", properties.getProperty("host", "localhost"));
    params.put("port", Integer.parseInt(properties.getProperty("port", "5432")));
    params.put("schema", properties.getProperty("schema", "public"));
    params.put("database", properties.getProperty("database", "baremaps"));
    params.put("user", properties.getProperty("user", "baremaps"));
    params.put("passwd", properties.getProperty("host", "baremaps"));
    DataStore dataStore = DataStoreFinder.getDataStore(params);
    dataStore.createSchema(SCHEMA);

    // Load features
    Transaction transaction = new DefaultTransaction("create");
    SimpleFeatureSource featureSource = dataStore.getFeatureSource(SCHEMA.getName().getLocalPart());
    if (featureSource instanceof SimpleFeatureStore) {
      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
      featureStore.setTransaction(transaction);
      try {
        featureStore.addFeatures(fc);
        transaction.commit();
      } catch (Exception e) {
        logger.error("Failed to load data", e);
        transaction.rollback();
        dataStore.removeSchema(SCHEMA.getName().getLocalPart());
        return Response.serverError().build();
      } finally {
        transaction.close();
      }
    } else {
      logger.error(SCHEMA.getName().getLocalPart() + " does not support read/write access");
      return Response.serverError().build();
    }

    // Register collection
    jdbi.useHandle(
        handle -> {
          handle
              .createUpdate("insert into collections (id, title) values (:id, :title)")
              .bind("id", collection.getId())
              .bind("title", collection.getTitle())
              .execute();
        });

    return Response.created(URI.create("collections/" + collection.getId())).build();
  }

  //  Stream<SimpleFeature>
  //  Stream<Geodata>
  //  org.geoapi.feature.simple.SimpleFeature
}
