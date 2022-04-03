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

package com.baremaps.blob;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import org.junit.jupiter.api.Test;

class BlobStoreRouterTest {

  @Test
  void addScheme() throws BlobStoreException {
    BlobStore a = mock(BlobStore.class);
    BlobStore b = mock(BlobStore.class);

    BlobStore router = new BlobStoreRouter().addScheme("a", a).addScheme("b", b);

    URI uri1 = URI.create("a://test/test1.txt");
    router.get(uri1);
    verify(a).get(uri1);

    URI uri2 = URI.create("b://test/test2.txt");
    router.get(uri2);
    verify(b).get(uri2);
  }

  @Test
  void head() throws BlobStoreException {
    BlobStore blobStore = mock(BlobStore.class);
    BlobStore router = new BlobStoreRouter().addScheme("blob", blobStore);
    URI uri = URI.create("blob://test/test.txt");
    router.head(uri);
    verify(blobStore).head(uri);
  }

  @Test
  void get() throws BlobStoreException {
    BlobStore blobStore = mock(BlobStore.class);
    BlobStore router = new BlobStoreRouter().addScheme("blob", blobStore);
    URI uri = URI.create("blob://test/test.txt");
    router.get(uri);
    verify(blobStore).get(uri);
  }

  @Test
  void put() throws BlobStoreException {
    BlobStore blobStore = mock(BlobStore.class);
    BlobStore router = new BlobStoreRouter().addScheme("blob", blobStore);
    URI uri = URI.create("blob://test/test.txt");
    router.put(uri, null);
    verify(blobStore).put(uri, null);
  }

  @Test
  void delete() throws BlobStoreException {
    BlobStore blobStore = mock(BlobStore.class);
    BlobStore router = new BlobStoreRouter().addScheme("blob", blobStore);
    URI uri = URI.create("blob://test/test.txt");
    router.delete(uri);
    verify(blobStore).delete(uri);
  }
}
