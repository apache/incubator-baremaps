package com.baremaps.store.memory;

import java.nio.ByteBuffer;

public interface Memory extends AutoCloseable {

  int segmentBytes();

  long segmentBits();

  long segmentMask();

  ByteBuffer segment(int index);

}
