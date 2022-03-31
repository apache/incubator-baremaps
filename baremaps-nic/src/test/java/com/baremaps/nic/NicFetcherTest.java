/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.nic;

import static com.baremaps.nic.NicFetcher.NIC_URLS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

class NicFetcherTest {

  @Test
  @Ignore
  void fetch() throws IOException {
    assertEquals(NIC_URLS.size(), new NicFetcher().fetch().count());
  }
}
