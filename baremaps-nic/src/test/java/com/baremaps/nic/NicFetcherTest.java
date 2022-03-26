package com.baremaps.nic;

import static com.baremaps.nic.NicFetcher.NIC_URLS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class NicFetcherTest {

  @Test
  @Disabled
  void fetch() throws IOException {
    assertEquals(NIC_URLS.size(), new NicFetcher().fetch().count());
  }
}