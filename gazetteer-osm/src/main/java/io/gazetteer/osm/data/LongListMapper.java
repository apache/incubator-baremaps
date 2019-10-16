package io.gazetteer.osm.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LongListMapper implements VariableSizeObjectMapper<List<Long>> {

  @Override
  public int size(List<Long> value) {
    return 4 + 8 * value.size();
  }

  @Override
  public List<Long> read(ByteBuffer buffer) {
    int size = buffer.getInt();
    List<Long> values = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      values.add(buffer.getLong());
    }
    return values;
  }

  @Override
  public void write(ByteBuffer buffer, List<Long> values) {
    buffer.putInt(values.size());
    for (Long value : values) {
      buffer.putLong(value);
    }
  }

}
