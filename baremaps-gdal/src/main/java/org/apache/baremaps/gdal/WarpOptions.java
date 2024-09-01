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

/**
 * Options for the warp method.
 */
public class WarpOptions extends Options {

  public WarpOptions() {
    super();
  }

  // Basic Options
  public WarpOptions help() {
    add("--help");
    return this;
  }

  public WarpOptions longUsage() {
    add("--long-usage");
    return this;
  }

  public WarpOptions helpGeneral() {
    add("--help-general");
    return this;
  }

  public WarpOptions quiet() {
    add("--quiet");
    return this;
  }

  public WarpOptions overwrite() {
    add("-overwrite");
    return this;
  }

  public WarpOptions format(String format) {
    add("-of");
    add(format);
    return this;
  }

  public WarpOptions creationOption(String name, String value) {
    add("-co");
    add(name + "=" + value);
    return this;
  }

  public WarpOptions sourceSRS(String srs) {
    add("-s_srs");
    add(srs);
    return this;
  }

  public WarpOptions targetSRS(String srs) {
    add("-t_srs");
    add(srs);
    return this;
  }

  public WarpOptions srcAlpha() {
    add("-srcalpha");
    return this;
  }

  public WarpOptions noSrcAlpha() {
    add("-nosrcalpha");
    return this;
  }

  public WarpOptions dstAlpha() {
    add("-dstalpha");
    return this;
  }

  public WarpOptions targetResolution(double xRes, double yRes) {
    add("-tr");
    add(xRes);
    add(yRes);
    return this;
  }

  public WarpOptions targetResolutionSquare(double res) {
    add("-tr");
    add(res);
    add(res);
    return this;
  }

  public WarpOptions targetSize(int width, int height) {
    add("-ts");
    add(width);
    add(height);
    return this;
  }

  public WarpOptions targetExtent(double xmin, double ymin, double xmax, double ymax) {
    add("-te");
    add(xmin);
    add(ymin);
    add(xmax);
    add(ymax);
    return this;
  }

  public WarpOptions targetExtentSRS(String srs) {
    add("-te_srs");
    add(srs);
    return this;
  }

  public WarpOptions resampling(String resampling) {
    add("-r");
    add(resampling);
    return this;
  }

  public WarpOptions outputFormat(String format) {
    add("-of");
    add(format);
    return this;
  }

  public WarpOptions srcDataset(String datasetName) {
    add(datasetName);
    return this;
  }

  public WarpOptions dstDataset(String datasetName) {
    add(datasetName);
    return this;
  }

  // Advanced Options
  public WarpOptions warpOption(String name, String value) {
    add("-wo");
    add(name + "=" + value);
    return this;
  }

  public WarpOptions multi() {
    add("-multi");
    return this;
  }

  public WarpOptions srcCoordEpoch(String epoch) {
    add("-s_coord_epoch");
    add(epoch);
    return this;
  }

  public WarpOptions tgtCoordEpoch(String epoch) {
    add("-t_coord_epoch");
    return this;
  }

  public WarpOptions coordinateTransformation(String ct) {
    add("-ct");
    add(ct);
    return this;
  }

  public WarpOptions useTPS() {
    add("-tps");
    return this;
  }

  public WarpOptions useRPC() {
    add("-rpc");
    return this;
  }

  public WarpOptions useGeolocation() {
    add("-geoloc");
    return this;
  }

  public WarpOptions order(int order) {
    add("-order");
    add(order);
    return this;
  }

  public WarpOptions refineGCPs(double tolerance, int minimumGCPs) {
    add("-refine_gcps");
    add(tolerance);
    add(minimumGCPs);
    return this;
  }

  public WarpOptions transformationOption(String name, String value) {
    add("-to");
    add(name + "=" + value);
    return this;
  }

  public WarpOptions errorThreshold(double threshold) {
    add("-et");
    add(threshold);
    return this;
  }

  public WarpOptions workingMemory(int memoryInMb) {
    add("-wm");
    add(memoryInMb);
    return this;
  }

  public WarpOptions srcNodata(double... values) {
    add("-srcnodata");
    add(join(values, " "));
    return this;
  }

  public WarpOptions dstNodata(double... values) {
    add("-dstnodata");
    add(join(values, " "));
    return this;
  }

  public WarpOptions targetAlignedPixels() {
    add("-tap");
    return this;
  }

  public WarpOptions warpType(String type) {
    add("-wt");
    add(type);
    return this;
  }

  public WarpOptions cutline(String cutline) {
    add("-cutline");
    add(cutline);
    return this;
  }

  public WarpOptions cutlineSRS(String srs) {
    add("-cutline_srs");
    add(srs);
    return this;
  }

  public WarpOptions cutlineWhere(String expression) {
    add("-cwhere");
    add(expression);
    return this;
  }

  public WarpOptions cutlineLayer(String layerName) {
    add("-cl");
    add(layerName);
    return this;
  }

  public WarpOptions cutlineSQL(String query) {
    add("-csql");
    add(query);
    return this;
  }

  public WarpOptions cutlineBlend(double distance) {
    add("-cblend");
    add(distance);
    return this;
  }

  public WarpOptions cropToCutline() {
    add("-crop_to_cutline");
    return this;
  }

  public WarpOptions noMetadata() {
    add("-nomd");
    return this;
  }

  public WarpOptions conflictValue(String value) {
    add("-cvmd");
    add(value);
    return this;
  }

  public WarpOptions setColorInterpretation() {
    add("-setci");
    return this;
  }

  public WarpOptions openOption(String name, String value) {
    add("-oo");
    add(name + "=" + value);
    return this;
  }

  public WarpOptions datasetOpenOption(String name, String value) {
    add("-doo");
    add(name + "=" + value);
    return this;
  }

  public WarpOptions overview(int level) {
    add("-ovr");
    add(level);
    return this;
  }

  public WarpOptions overviewAuto() {
    add("-ovr");
    add("AUTO");
    return this;
  }

  public WarpOptions overviewNone() {
    add("-ovr");
    add("NONE");
    return this;
  }

  public WarpOptions verticalShift() {
    add("-vshift");
    return this;
  }

  public WarpOptions noVerticalShiftGrid() {
    add("-novshiftgrid");
    return this;
  }

  public WarpOptions inputFormat(String format) {
    add("-if");
    add(format);
    return this;
  }

  public WarpOptions srcBand(int n) {
    add("-srcband");
    add(n);
    return this;
  }

  public WarpOptions dstBand(int n) {
    add("-dstband");
    add(n);
    return this;
  }

  // Helper method to join array elements
  private String join(double[] array, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      sb.append(array[i]);
      if (i < array.length - 1) {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }
}
