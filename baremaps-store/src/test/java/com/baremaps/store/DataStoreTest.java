package com.baremaps.store;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.IntDataType;
import com.baremaps.store.type.IntListDataType;
import java.util.ArrayList;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DataStoreTest {

  @Test
  public void addFixedSizeData() {
    var store = new DataStore<>(new IntDataType(), new OffHeapMemory(1 << 10));
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i << 2, store.add(i));
    }
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i, store.get(i << 2));
    }
  }

  @Test
  public void addVariableSizeValues() {
    var store = new DataStore<>(new IntListDataType(), new OffHeapMemory(1 << 10));
    var random = new Random(0);
    var positions = new ArrayList<Long>();
    var values = new ArrayList<ArrayList<Integer>>();
    for (int i = 0; i < 1 <<20; i++) {
      var size = random.nextInt(10);
      var value = new ArrayList<Integer>();
      for (int j = 0; j < size; j++) {
        value.add(random.nextInt(1 << 20));
      }
      positions.add(store.add(value));
      values.add(value);
    }
    for (int i = 0; i < positions.size(); i++) {
      var value = store.get(positions.get(i));
      assertEquals(values.get(i), value);
    }
  }

}