/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.ogcapi;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

public class ApiResourceTest extends OgcApiTest {

  @Test
  @Tag("integration")
  public void getSwaggerUI() {
    var response = target().path("/swagger").request().get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.valueOf("text/html"), response.getMediaType());
  }

  @Test
  @Tag("integration")
  public void getJsonSpecification() {
    var response = target()
        .path("/api")
        .request()
        .header("mimeType", "application/json")
        .get();
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.valueOf("application/json"), response.getMediaType());
  }
}
