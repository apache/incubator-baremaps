package com.baremaps.iploc.nic;

import java.io.IOException;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NicFetcherTest {

  @Test
  @Ignore
  void fetch() throws IOException {
    Assertions.assertEquals(NicFetcher.NIC_URLS.size(), new NicFetcher().fetch().count());
  }
}
