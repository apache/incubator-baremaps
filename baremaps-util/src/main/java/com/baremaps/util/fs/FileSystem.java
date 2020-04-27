/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.util.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public abstract class FileSystem {

  public abstract boolean accept(URI uri);

  public abstract InputStream read(URI uri) throws IOException;

  public abstract byte[] readByteArray(URI uri) throws IOException;

  public abstract OutputStream write(URI uri) throws IOException;

  public abstract void writeByteArray(URI uri, byte[] bytes) throws IOException;

  public abstract void delete(URI uri) throws IOException;

}
