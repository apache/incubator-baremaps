package io.gazetteer.postgis;

import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

public class PGGeometry extends PGobject implements PGBinaryObject {

  @Override
  public void setByteValue(byte[] value, int offset) {

  }

  @Override
  public int lengthInBytes() {
    return 0;
  }

  @Override
  public void toBytes(byte[] bytes, int offset) {

  }

}
