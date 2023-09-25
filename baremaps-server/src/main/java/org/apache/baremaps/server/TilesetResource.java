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

package org.apache.baremaps.server;

import org.apache.baremaps.vectortile.tileset.Tileset;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

/**
 * A resource that provides access to the tileset file. Only suitable for development purposes, as
 * it exposes SQL queries.
 */
@Singleton
@javax.ws.rs.Path("/")
public class TilesetResource {

  private final Supplier<Tileset> tilesetSupplier;

  @Inject
  public TilesetResource(Supplier<Tileset> tilesetSupplier) {
    this.tilesetSupplier = tilesetSupplier;
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Tileset getTileset() {
    var tileset = tilesetSupplier.get();
    // Hide the database connection
    tileset.setDatabase(null);
    return tileset;
  }

}
