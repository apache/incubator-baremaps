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

package org.apache.baremaps.raster.gdal;

public class BuildVRTOptions extends Options {

  public BuildVRTOptions() {
    super();
  }

  // Basic Options
  public BuildVRTOptions help() {
    add("--help");
    return this;
  }

  public BuildVRTOptions helpGeneral() {
    add("--help-general");
    return this;
  }

  public BuildVRTOptions tileIndex(String fieldName) {
    add("-tileindex");
    add(fieldName);
    return this;
  }

  public BuildVRTOptions resolution(String resolution) {
    add("-resolution");
    add(resolution);
    return this;
  }

  public BuildVRTOptions targetExtent(double xmin, double ymin, double xmax, double ymax) {
    add("-te");
    add(xmin);
    add(ymin);
    add(xmax);
    add(ymax);
    return this;
  }

  public BuildVRTOptions targetResolution(double xres, double yres) {
    add("-tr");
    add(xres);
    add(yres);
    return this;
  }

  public BuildVRTOptions targetAlignedPixels() {
    add("-tap");
    return this;
  }

  public BuildVRTOptions separate() {
    add("-separate");
    return this;
  }

  public BuildVRTOptions band(int band) {
    add("-b");
    add(band);
    return this;
  }

  public BuildVRTOptions subdataset(int n) {
    add("-sd");
    add(n);
    return this;
  }

  public BuildVRTOptions allowProjectionDifference() {
    add("-allow_projection_difference");
    return this;
  }

  public BuildVRTOptions quiet() {
    add("-q");
    return this;
  }

  public BuildVRTOptions addAlpha() {
    add("-addalpha");
    return this;
  }

  public BuildVRTOptions hideNodata() {
    add("-hidenodata");
    return this;
  }

  public BuildVRTOptions sourceNodata(String... values) {
    add("-srcnodata");
    add(String.join(" ", values));
    return this;
  }

  public BuildVRTOptions vrtNodata(String... values) {
    add("-vrtnodata");
    add(String.join(" ", values));
    return this;
  }

  public BuildVRTOptions ignoreSrcMaskBand() {
    add("-ignore_srcmaskband");
    return this;
  }

  public BuildVRTOptions nodataMaxMaskThreshold(double threshold) {
    add("-nodata_max_mask_threshold");
    add(threshold);
    return this;
  }

  public BuildVRTOptions assignSRS(String srs) {
    add("-a_srs");
    add(srs);
    return this;
  }

  public BuildVRTOptions resamplingMethod(String method) {
    add("-r");
    add(method);
    return this;
  }

  public BuildVRTOptions openOption(String name, String value) {
    add("-oo");
    add(name + "=" + value);
    return this;
  }

  public BuildVRTOptions inputFileList(String filename) {
    add("-input_file_list");
    add(filename);
    return this;
  }

  public BuildVRTOptions overwrite() {
    add("-overwrite");
    return this;
  }

  public BuildVRTOptions strict() {
    add("-strict");
    return this;
  }

  public BuildVRTOptions nonStrict() {
    add("-non_strict");
    return this;
  }

  public BuildVRTOptions outputFilename(String filename) {
    add(filename);
    return this;
  }

  public BuildVRTOptions inputRaster(String... rasters) {
    for (String raster : rasters) {
      add(raster);
    }
    return this;
  }
}
