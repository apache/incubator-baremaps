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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * An abstraction to read and write blobs identified by URIs.
 */
public interface BlobStore {

  long size(URI uri) throws IOException;

  InputStream read(URI uri) throws IOException;

  byte[] readByteArray(URI uri) throws IOException;

  OutputStream write(URI uri) throws IOException;

  OutputStream write(URI uri, Map<String, String> metadata) throws IOException;

  void writeByteArray(URI uri, byte[] bytes) throws IOException;

  void writeByteArray(URI uri, byte[] bytes, Map<String, String> metadata) throws IOException;

  void delete(URI uri) throws IOException;

}
