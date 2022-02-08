package com.baremaps.store;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.FixedSizeDataType;
import com.baremaps.store.type.IntDataType;
import com.baremaps.store.type.LongDataType;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class FixedSizeDataListTest {

  @Test
  public void smallSegments() {
    assertThrows(RuntimeException.class, () -> new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory(4)));
  }

  @Test
  public void misalignedSegments() {
    assertThrows(RuntimeException.class,() -> {
      new FixedSizeDataList<>(new FixedSizeDataType<>() {
        @Override
        public int size(Object value) {
          return 3;
        }

        @Override
        public void write(ByteBuffer buffer, int position, Object value) {

        }

        @Override
        public Object read(ByteBuffer buffer, int position) {
          return null;
        }
      }, new OffHeapMemory(16));
    });
  }

  @Test
  public void appendFixedSizeValues() {
    var list = new FixedSizeDataList<>(new IntDataType(), new OffHeapMemory(1 << 10));
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i, list.add(i));
    }
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i, list.get(i));
    }
  }

}