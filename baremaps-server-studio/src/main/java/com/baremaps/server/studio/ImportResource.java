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
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdbi.v3.core.Jdbi;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("")
public class ImportResource {

  private static final Logger logger = LoggerFactory.getLogger(ImportResource.class);

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
    var fc = fjson.readFeatureCollection(fileInputStream);
    SimpleFeatureType schema = (SimpleFeatureType) fc.getSchema();

    // Build FeatureType
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(collection.getId().toString());
    builder.setCRS(schema.getCoordinateReferenceSystem()); // <- Coordinate reference system
    builder.addAll(schema.getAttributeDescriptors());
    SimpleFeatureType SCHEMA = builder.buildFeatureType();

    // Setup DataStore
    URI uri =
        jdbi.withHandle(
            haandle -> {
              String url = haandle.getConnection().getMetaData().getURL();
              logger.debug(url);
              return URI.create(url.substring(5));
            });
    Map<String, String> query =
        URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.toString()).stream()
            .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
    Map<String, Object> params = new HashMap<>();
    params.put("dbtype", "postgis");
    params.put("host", uri.getHost());
    params.put("port", uri.getPort());
    params.put("schema", query.getOrDefault("currentSchema", "public"));
    params.put("database", uri.getPath().substring(1));
    params.put("user", query.getOrDefault("user", "postgres"));
    params.put("passwd", query.getOrDefault("password", "postgres"));
    params.forEach((key, value) -> logger.debug("Params: " + key + " -> " + value));
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
        // make schema compatible for tile query
        String sql =
            String.format(
                "alter table \"%1$s\" rename fid to id;"
                    + "alter table \"%1$s\" add column tags hstore, add column geom geometry;"
                    + "update \"%1$s\" set geom = ST_Transform(ST_SetSRID(geometry, 4326), 3857);"
                    + "alter table \"%1$s\" drop column geometry;",
                collection.getId());
        jdbi.useHandle(handle -> handle.execute(sql));
      }
    } else {
      logger.error(SCHEMA.getName().getLocalPart() + " does not support read/write access");
      return Response.serverError().build();
    }

    // Register collection
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate("insert into collections (id, title) values (:id, :title)")
                .bind("id", collection.getId())
                .bind("title", collection.getTitle())
                .execute());

    return Response.created(URI.create("collections/" + collection.getId())).build();
  }

  //  Stream<SimpleFeature>
  //  Stream<Geodata>
  //  org.geoapi.feature.simple.SimpleFeature
}
