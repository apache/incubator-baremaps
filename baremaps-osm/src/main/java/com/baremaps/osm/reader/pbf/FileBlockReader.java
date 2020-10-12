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
package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.reader.Reader;
import com.baremaps.osm.reader.ReaderException;
import com.baremaps.osm.stream.StreamException;
import java.io.IOException;
import java.nio.file.Path;

public class FileBlockReader implements Reader<FileBlockHandler> {

  public void read(Path path, FileBlockHandler handler) throws ReaderException {
    try {
      new FileBlockStreamer().stream(path, true, true)
          .forEach(block -> handle(block, handler));
    } catch (StreamException e) {
      throw new ReaderException(e.getCause());
    } catch (IOException e) {
      throw new ReaderException(e);
    }
  }

  private void handle(FileBlock block, FileBlockHandler handler) {
    try {
      if (block instanceof HeaderBlock) {
        handler.onHeaderBlock((HeaderBlock) block);
      } else if (block instanceof DataBlock) {
        handler.onDataBlock((DataBlock) block);
      }
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

}
