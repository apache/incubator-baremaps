package com.baremaps.openapi.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.model.MbStyle;
import com.baremaps.model.StyleSet;
import com.baremaps.postgres.jdbi.PostgisPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Test;

public class StylesServiceTest extends JerseyTest {

  Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    // Create a connection to a throwaway postgis database
    Connection connection;
    try {
      connection = DriverManager.getConnection("jdbc:tc:postgis:13-3.1:///test");
    } catch (SQLException throwables) {
      throw new RuntimeException("Unable to connect to the database");
    }

    // Initialize the database
    jdbi = Jdbi.create(connection)
        .installPlugin(new PostgisPlugin())
        .installPlugin(new Jackson2Plugin());
    jdbi.useHandle(handle -> {
      handle.execute("create table styles (id varchar primary key, style jsonb)");
    });

    // Configure the service
    return new ResourceConfig()
        .register(StylesService.class)
        .register(new AbstractBinder() {
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
    assertEquals(0, styles.getStyles().size());

    // Create a new style with the service
    MbStyle style = new MbStyle();
    style.setName("test");
    target().path("/styles")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(style, MediaType.valueOf("application/vnd.mapbox.style+json")));

    // List the styles
    styles = target().path("/styles").request().get(StyleSet.class);
    assertEquals(1, styles.getStyles().size());

    // Get the style
    String id = styles.getStyles().get(0).getId();
    style = target().path("/styles/" + id).request().get(MbStyle.class);
    assertEquals("test", style.getName());

    // Delete the style
    target().path("/styles/" + styles.getStyles().get(0).getId()).request().delete();

    // List the styles
    styles = target().path("/styles").request().get(StyleSet.class);
    assertEquals(0, styles.getStyles().size());
  }

}