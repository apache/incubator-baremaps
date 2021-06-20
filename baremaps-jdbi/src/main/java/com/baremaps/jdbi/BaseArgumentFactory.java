package com.baremaps.jdbi;

import com.baremaps.osm.geometry.GeometryUtil;
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
    return (position, statement, ctx) -> statement.setBytes(position, GeometryUtil.serialize(value));
  }

}
