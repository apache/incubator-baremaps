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
 * Options for the translate method.
 */
public class TranslateOptions extends Options {

  public TranslateOptions() {
    super();
  }

  // Basic Options
  public TranslateOptions help() {
    add("--help");
    return this;
  }

  public TranslateOptions helpGeneral() {
    add("--help-general");
    return this;
  }

  public TranslateOptions longUsage() {
    add("--long-usage");
    return this;
  }

  public TranslateOptions outputType(String type) {
    add("-ot");
    add(type);
    return this;
  }

  public TranslateOptions strict() {
    add("-strict");
    return this;
  }

  public TranslateOptions inputFormat(String format) {
    add("-if");
    add(format);
    return this;
  }

  public TranslateOptions outputFormat(String format) {
    add("-of");
    add(format);
    return this;
  }

  public TranslateOptions band(int band) {
    add("-b");
    add(band);
    return this;
  }

  public TranslateOptions maskBand(int band) {
    add("-mask");
    add(band);
    return this;
  }

  public TranslateOptions expand(String option) {
    add("-expand");
    add(option);
    return this;
  }

  public TranslateOptions outsize(int xSize, int ySize) {
    add("-outsize");
    add(xSize);
    add(ySize);
    return this;
  }

  public TranslateOptions outsizePercent(int xPercent, int yPercent) {
    add("-outsize");
    add(xPercent + "%");
    add(yPercent + "%");
    return this;
  }

  public TranslateOptions targetResolution(double xRes, double yRes) {
    add("-tr");
    add(xRes);
    add(yRes);
    return this;
  }

  public TranslateOptions overview(int level) {
    add("-ovr");
    add(level);
    return this;
  }

  public TranslateOptions overviewAuto() {
    add("-ovr");
    add("AUTO");
    return this;
  }

  public TranslateOptions overviewNone() {
    add("-ovr");
    add("NONE");
    return this;
  }

  public TranslateOptions resamplingMethod(String method) {
    add("-r");
    add(method);
    return this;
  }

  public TranslateOptions unscale() {
    add("-unscale");
    return this;
  }

  public TranslateOptions scale(int band, double srcMin, double srcMax, double dstMin,
      double dstMax) {
    add("-scale_" + band);
    add(srcMin);
    add(srcMax);
    add(dstMin);
    add(dstMax);
    return this;
  }

  public TranslateOptions scale(double srcMin, double srcMax, double dstMin, double dstMax) {
    add("-scale");
    add(srcMin);
    add(srcMax);
    add(dstMin);
    add(dstMax);
    return this;
  }

  public TranslateOptions exponent(int band, double expVal) {
    add("-exponent_" + band);
    add(expVal);
    return this;
  }

  public TranslateOptions exponent(double expVal) {
    add("-exponent");
    add(expVal);
    return this;
  }

  public TranslateOptions srcWindow(int xOff, int yOff, int xSize, int ySize) {
    add("-srcwin");
    add(xOff);
    add(yOff);
    add(xSize);
    return this;
  }

  public TranslateOptions errorOnPartiallyOutside() {
    add("-epo");
    return this;
  }

  public TranslateOptions errorOnCompletelyOutside() {
    add("-eco");
    return this;
  }

  public TranslateOptions projectionWindow(double ulx, double uly, double lrx, double lry) {
    add("-projwin");
    add(ulx);
    add(uly);
    add(lrx);
    add(lry);
    return this;
  }

  public TranslateOptions projectionWindowSRS(String srs) {
    add("-projwin_srs");
    add(srs);
    return this;
  }

  public TranslateOptions assignSRS(String srs) {
    add("-a_srs");
    add(srs);
    return this;
  }

  public TranslateOptions assignCoordEpoch(String epoch) {
    add("-a_coord_epoch");
    add(epoch);
    return this;
  }

  public TranslateOptions assignGeoTransform(double gt0, double gt1, double gt2, double gt3,
      double gt4, double gt5) {
    add("-a_gt");
    add(gt0);
    add(gt1);
    add(gt2);
    add(gt3);
    add(gt4);
    add(gt5);
    return this;
  }

  public TranslateOptions assignNodataValue(double value) {
    add("-a_nodata");
    add(value);
    return this;
  }

  public TranslateOptions assignScale(double value) {
    add("-a_scale");
    add(value);
    return this;
  }

  public TranslateOptions assignOffset(double value) {
    add("-a_offset");
    add(value);
    return this;
  }

  public TranslateOptions noGCPs() {
    add("-nogcp");
    return this;
  }

  public TranslateOptions gcp(double pixel, double line, double easting, double northing,
      Double elevation) {
    add("-gcp");
    add(pixel);
    add(line);
    add(easting);
    add(northing);
    if (elevation != null) {
      add(elevation);
    }
    return this;
  }

  public TranslateOptions colorInterpretation(String... interpretations) {
    add("-colorinterp");
    for (String interp : interpretations) {
      add(interp);
    }
    return this;
  }

  public TranslateOptions metadataOption(String name, String value) {
    add("-mo");
    add(name + "=" + value);
    return this;
  }

  public TranslateOptions domainMetadataOption(String domain, String name, String value) {
    add("-dmo");
    add(domain + ":" + name + "=" + value);
    return this;
  }

  public TranslateOptions quiet() {
    add("-q");
    return this;
  }

  public TranslateOptions subdatasets() {
    add("-sds");
    return this;
  }

  public TranslateOptions creationOption(String name, String value) {
    add("-co");
    add(name + "=" + value);
    return this;
  }

  public TranslateOptions statistics() {
    add("-stats");
    return this;
  }

  public TranslateOptions noRAT() {
    add("-norat");
    return this;
  }

  public TranslateOptions noXMP() {
    add("-noxmp");
    return this;
  }

  public TranslateOptions openOption(String name, String value) {
    add("-oo");
    add(name + "=" + value);
    return this;
  }

  public TranslateOptions srcDataset(String srcDataset) {
    add(srcDataset);
    return this;
  }

  public TranslateOptions dstDataset(String dstDataset) {
    add(dstDataset);
    return this;
  }
}
