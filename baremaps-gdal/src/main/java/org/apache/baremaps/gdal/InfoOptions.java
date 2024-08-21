/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.gdal;

public class InfoOptions extends Options {

  public InfoOptions() {
    super();
  }

  // Basic Options
  public InfoOptions help() {
    add("--help");
    return this;
  }

  public InfoOptions helpGeneral() {
    add("--help-general");
    return this;
  }

  public InfoOptions json() {
    add("-json");
    return this;
  }

  public InfoOptions minMax() {
    add("-mm");
    return this;
  }

  public InfoOptions stats() {
    add("-stats");
    return this;
  }

  public InfoOptions approxStats() {
    add("-approx_stats");
    return this;
  }

  public InfoOptions histogram() {
    add("-hist");
    return this;
  }

  public InfoOptions noGCP() {
    add("-nogcp");
    return this;
  }

  public InfoOptions noMetadata() {
    add("-nomd");
    return this;
  }

  public InfoOptions noRAT() {
    add("-norat");
    return this;
  }

  public InfoOptions noColorTable() {
    add("-noct");
    return this;
  }

  public InfoOptions noFileList() {
    add("-nofl");
    return this;
  }

  public InfoOptions noNodata() {
    add("-nonodata");
    return this;
  }

  public InfoOptions noMask() {
    add("-nomask");
    return this;
  }

  public InfoOptions checksum() {
    add("-checksum");
    return this;
  }

  public InfoOptions listMDD() {
    add("-listmdd");
    return this;
  }

  public InfoOptions metadataDomain(String domain) {
    add("-mdd");
    add(domain);
    return this;
  }

  public InfoOptions proj4() {
    add("-proj4");
    return this;
  }

  public InfoOptions wktFormat(String format) {
    add("-wkt_format");
    add(format);
    return this;
  }

  public InfoOptions subdataset(int sdIndex) {
    add("-sd");
    add(sdIndex);
    return this;
  }

  public InfoOptions openOption(String name, String value) {
    add("-oo");
    add(name + "=" + value);
    return this;
  }

  public InfoOptions inputFormat(String format) {
    add("-if");
    add(format);
    return this;
  }

  public InfoOptions datasetName(String dataset) {
    add(dataset);
    return this;
  }
}
