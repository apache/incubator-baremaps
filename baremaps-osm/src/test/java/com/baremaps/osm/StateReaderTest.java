package com.baremaps.osm;

import static org.junit.jupiter.api.Assertions.*;

import com.baremaps.osm.domain.State;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class StateReaderTest {

  @Test
  void read() throws URISyntaxException, IOException {
    InputStream inputStream = Files.newInputStream(TestFiles.monocoStateTxt());
    State state = new StateReader(inputStream).read();
    assertEquals(2788, state.getSequenceNumber());
    assertEquals(LocalDateTime.parse("2020-11-10T21:42:03"), state.getTimestamp());
  }

}