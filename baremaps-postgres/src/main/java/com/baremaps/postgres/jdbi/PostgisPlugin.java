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
package com.baremaps.postgres.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;

public class PostgisPlugin extends JdbiPlugin.Singleton {

  @Override
  public void customizeJdbi(Jdbi jdbi) {
    // Register argument factories
    jdbi.registerArgument(new PointArgumentFactory());
    jdbi.registerArgument(new LineStringArgumentFactory());
    jdbi.registerArgument(new LinearRingArgumentFactory());
    jdbi.registerArgument(new PolygonArgumentFactory());
    jdbi.registerArgument(new MultiPointArgumentFactory());
    jdbi.registerArgument(new MultiLineStringArgumentFactory());
    jdbi.registerArgument(new MultiPolygonArgumentFactory());
    jdbi.registerArgument(new GeometryCollectionArgumentFactory());
    jdbi.registerArgument(new GeometryArgumentFactory());

    // Register column mappers
    jdbi.registerColumnMapper(new PointColumnMapper());
    jdbi.registerColumnMapper(new LineStringColumnMapper());
    jdbi.registerColumnMapper(new LinearRingColumnMapper());
    jdbi.registerColumnMapper(new PolygonColumnMapper());
    jdbi.registerColumnMapper(new MultiPointColumnMapper());
    jdbi.registerColumnMapper(new MultiLineStringColumnMapper());
    jdbi.registerColumnMapper(new MultiPolygonColumnMapper());
    jdbi.registerColumnMapper(new GeometryCollectionColumnMapper());
    jdbi.registerColumnMapper(new GeometryColumnMapper());
  }
}
