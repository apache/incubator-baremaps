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



import java.util.Arrays;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import org.apache.baremaps.api.ConformanceApi;
import org.apache.baremaps.model.ConfClasses;

@Singleton
public class ConformanceResource implements ConformanceApi {

  @Override
  public Response getConformanceDeclaration() {
    ConfClasses confClasses = new ConfClasses();
    confClasses
        .setConformsTo(Arrays.asList("http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/core",
            "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/core",
            "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/json",
            "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/manage-styles",
            "http://www.opengis.net/spec/ogcapi-styles-1/1.0/conf/mapbox-styles",
            "http://www.opengis.net/spec/ogcapi-tiles-1/1.0/conf/core",
            "http://www.opengis.net/spec/ogcapi-tiles-1/1.0/conf/tileset"));

    return Response.ok().entity(confClasses).build();
  }
}
