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



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.baremaps.model.MbStyle;
import org.apache.baremaps.model.StyleSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Assert;
import org.junit.Test;

public class StylesResourceIntegrationTest extends JerseyTest {

  Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    // Create a connection to a throwaway postgres database
    Connection connection;
    try {
      connection = DriverManager.getConnection("jdbc:tc:postgresql:13:///test");
    } catch (SQLException throwables) {
      throw new RuntimeException("Unable to connect to the database");
    }

    // Initialize the database
    jdbi = Jdbi.create(connection).installPlugin(new Jackson2Plugin());
    jdbi.useHandle(
        handle -> handle.execute("create table styles (id uuid primary key, style jsonb)"));

    // Configure the service
    return new ResourceConfig().register(StylesResource.class).register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(jdbi).to(Jdbi.class);
      }
    });
  }

  @Test
  public void test() {
    // List the styles
    StyleSet styles = target().path("/styles").request().get(StyleSet.class);
    Assert.assertEquals(0, styles.getStyles().size());

    // Create a new style with the service
    MbStyle style = new MbStyle();
    style.setName("test");
    target().path("/styles").request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(style, MediaType.valueOf("application/vnd.mapbox.style+json")));

    // List the styles
    styles = target().path("/styles").request().get(StyleSet.class);
    Assert.assertEquals(1, styles.getStyles().size());

    // Get the style
    UUID id = styles.getStyles().get(0).getId();
    style = target().path("/styles/" + id).request().get(MbStyle.class);
    Assert.assertEquals("test", style.getName());

    // Delete the style
    target().path("/styles/" + styles.getStyles().get(0).getId()).request().delete();

    // List the styles
    styles = target().path("/styles").request().get(StyleSet.class);
    Assert.assertEquals(0, styles.getStyles().size());
  }
}
