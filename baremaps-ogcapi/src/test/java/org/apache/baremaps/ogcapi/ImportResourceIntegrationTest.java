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

import static org.junit.Assert.assertEquals;

import com.google.common.io.Resources;
import java.io.File;
import java.net.URL;
import javax.sql.DataSource;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.baremaps.database.PostgresUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Ignore;
import org.junit.Test;

public class ImportResourceIntegrationTest extends JerseyTest {

  Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    // Create a datasource to a throwaway postgis database
    DataSource dataSource = PostgresUtils.dataSource("jdbc:tc:postgis:13-3.1:///baremaps");

    // Initialize the database
    jdbi = Jdbi.create(dataSource).installPlugin(new Jackson2Plugin())
        .installPlugin(new PostgisPlugin());
    jdbi.useHandle(handle -> handle.execute("create extension if not exists hstore;"
        + "create table collections (id uuid primary key, collection jsonb)"));

    // Configure the service
    return new ResourceConfig().registerClasses(MultiPartFeature.class, ImportResource.class)
        .register(new AbstractBinder() {
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

  @Test
  @Ignore("Geotools has been replaced with Apache SIS")
  public void test() {
    String FILE = "features.geojson";
    URL url = Resources.getResource(FILE);
    File data = new File(url.getFile());
    FileDataBodyPart fileDataBodyPart =
        new FileDataBodyPart("file", data, MediaType.APPLICATION_JSON_TYPE);
    MultiPart entity = new FormDataMultiPart().bodyPart(fileDataBodyPart);
    Response response = target().path("studio/import").request()
        .header("Content-Disposition", "form-data; name=\"file\"; fileName=\"features.geojson\"")
        .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
    assertEquals(201, response.getStatus());
  }
}
