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

import com.baremaps.osm.geometry.GeometryUtils;
import java.sql.Types;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.locationtech.jts.geom.Geometry;

abstract class BaseArgumentFactory<T extends Geometry> extends AbstractArgumentFactory<T> {

  public BaseArgumentFactory() {
    super(Types.OTHER);
  }

  @Override
  public Argument build(T value, ConfigRegistry config) {
    return (position, statement, ctx) ->
        statement.setBytes(position, GeometryUtils.serialize(value));
  }
}
