package org.apache.baremaps.openstreetmap.state;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StateReaderTest {

    @Test
    @Ignore
    void getStateFromTimestamp() {
        var reader = new StateReader();
        var state = reader.getStateFromTimestamp(LocalDateTime.now().minusDays(10));
        System.out.println(state.get().getSequenceNumber());
    }
}