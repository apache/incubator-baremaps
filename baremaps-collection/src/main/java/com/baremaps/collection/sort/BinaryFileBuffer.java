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

package com.baremaps.collection.sort;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * This is essentially a thin wrapper on top of a BufferedReader... which keeps the last line in
 * memory.
 */
public final class BinaryFileBuffer implements IOStringStack {
  public BinaryFileBuffer(BufferedReader r) throws IOException {
    this.fbr = r;
    reload();
  }

  public void close() throws IOException {
    this.fbr.close();
  }

  public boolean empty() {
    return this.cache == null;
  }

  public String peek() {
    return this.cache;
  }

  public String pop() throws IOException {
    String answer = peek().toString(); // make a copy
    reload();
    return answer;
  }

  private void reload() throws IOException {
    this.cache = this.fbr.readLine();
  }

  private BufferedReader fbr;

  private String cache;
}
