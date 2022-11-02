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

package org.apache.baremaps.server;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class BaseUriRequestFilterIntegrationTest extends JerseyTest {

  @Path("")
  public static class BaseUriService {
    @GET
    public String getBaseUri(@Context UriInfo uriInfo) {
      return uriInfo.getBaseUri().toString();
    }
  }

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return new ResourceConfig().registerClasses(BaseUriRequestFilter.class, BaseUriService.class);
  }

  @Test
  public void testBaseUri() {
    String baseUriStr = target().path("").request().header("X-Forwarded-Host", "test.com")
        .header("X-Forwarded-Proto", "https").header("X-Forwarded-Port", "443").get(String.class);
    assertEquals("https://test.com:443/", baseUriStr);
  }
}
