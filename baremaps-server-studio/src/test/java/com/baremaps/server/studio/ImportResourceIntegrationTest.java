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

import static com.baremaps.testing.TestConstants.DATABASE_URL;

import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.postgres.jdbi.PostgisPlugin;
import javax.sql.DataSource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;

public class ImportResourceIntegrationTest extends JerseyTest {

  Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    // Create a datasource to a throwaway postgis database
    DataSource dataSource = PostgresUtils.datasource(DATABASE_URL);

    // Initialize the database
    jdbi =
        Jdbi.create(dataSource)
            .installPlugin(new Jackson2Plugin())
            .installPlugin(new PostgisPlugin());
    jdbi.useHandle(
        handle ->
            handle.execute(
                "create table collections (id uuid primary key, title text, description text, links jsonb[] default '{}'::jsonb[], extent jsonb, item_type text default 'feature', crs text[])"));

    // Configure the service
    return new ResourceConfig()
        .registerClasses(MultiPartFeature.class, ImportResource.class)
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(jdbi).to(Jdbi.class);
              }
            });
  }

  @Override
  protected void configureClient(ClientConfig clientConfig) {
    clientConfig.register(MultiPartFeature.class);
  }

  //  @Test
  //  public void test() {
  //    String FILE = "features.geojson";
  //    URL url = Resources.getResource(FILE);
  //    File data = new File(url.getFile());
  //    FileDataBodyPart fileDataBodyPart =
  //        new FileDataBodyPart("file", data, MediaType.APPLICATION_JSON_TYPE);
  //    MultiPart entity = new FormDataMultiPart().bodyPart(fileDataBodyPart);
  //    Response response =
  //        target()
  //            .path("studio/import")
  //            .request()
  //            .header(
  //                "Content-Disposition", "form-data; name=\"file\";
  // fileName=\"features.geojson\"")
  //            .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
  //    // TODO: fix test
  //    assertEquals(201, response.getStatus());
  //  }

}
