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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;
import org.gdal.gdal.gdal;

public class Gdal {

  private Gdal() {
    // Prevent instantiation
  }

  public static void initialize() {
    gdal.AllRegister();
    gdal.UseExceptions();
  }

  public static Dataset open(Path path) {
    return open(path.normalize().toAbsolutePath().toString());
  }

  public static Dataset open(String path) {
    if (Files.notExists(Path.of(path))) {
      throw new IllegalArgumentException("File not found: " + path);
    }
    var dataset = gdal.OpenEx(path);
    return new Dataset(dataset);
  }

  public static Dataset copy(String path, Dataset source) {
    var driver = gdal.IdentifyDriver(path);
    return new Dataset(driver.CreateCopy(path, source.dataset));
  }

  public static String info(Dataset dataset) {
    return org.gdal.gdal.gdal.GDALInfo(dataset.dataset, null);
  }

  public static String info(Dataset dataset, InfoOptions options) {
    try (var infoOptions = new InfoResource(options)) {
      return org.gdal.gdal.gdal.GDALInfo(dataset.dataset, infoOptions);
    }
  }

  public static Dataset buildVRT(List<String> files, BuildVRTOptions options) {
    var buildVRTOptions = new org.gdal.gdal.BuildVRTOptions(options.asVector());
    try (var buildVRTResource = new BuildVRTResource(options)) {
      var target = org.gdal.gdal.gdal.BuildVRT(
          "",
          new Vector<>(files),
          buildVRTResource);
      return new Dataset(target);
    } finally {
      buildVRTOptions.delete();
    }
  }

  public static Dataset buildVRT(List<String> files, BuildVRTOptions options,
      ProgressCallback progressListener) {
    try (var buildVRTResource = new BuildVRTResource(options);
        var progressResource = new ProgressResource(progressListener)) {
      org.gdal.gdal.Dataset target = org.gdal.gdal.gdal.BuildVRT(
          "",
          new Vector<>(files),
          buildVRTResource,
          progressResource);
      return new Dataset(target);
    }
  }

  public static Dataset translate(Dataset source, TranslateOptions options) {
    try (var translateResource = new TranslateResource(options)) {
      var dataset = org.gdal.gdal.gdal.Translate(
          "",
          source.dataset,
          translateResource);
      return new Dataset(dataset);
    }
  }

  public static Dataset translate(Dataset source, TranslateOptions options,
      ProgressCallback progressListener) {
    try (var translateResource = new TranslateResource(options);
        var progressResource = new ProgressResource(progressListener)) {
      var dataset = org.gdal.gdal.gdal.Translate(
          "",
          source.dataset,
          translateResource,
          progressResource);
      return new Dataset(dataset);
    }
  }

  public static Dataset warp(Dataset source, WarpOptions options) {
    try (var warpResource = new WarpResource(options)) {
      var target = gdal.Warp(
          "",
          new org.gdal.gdal.Dataset[] {source.dataset},
          warpResource);
      return new Dataset(target);
    }
  }

  public static Dataset warp(Dataset source, WarpOptions options,
      ProgressCallback progressCallback) {
    try (var warpResource = new WarpResource(options);
        var progressResource = new ProgressResource(progressCallback)) {
      var target = gdal.Open("MEM");
      gdal.Warp(
          target,
          new org.gdal.gdal.Dataset[] {source.dataset},
          warpResource,
          progressResource);
      return new Dataset(target);
    }
  }

  public static void warp(Dataset target, List<Dataset> source, WarpOptions options) {
    gdal.Warp(
        target.dataset,
        source.stream().map(d -> d.dataset).toArray(org.gdal.gdal.Dataset[]::new),
        new WarpResource(options));
  }

  public static void main(String[] args) {
    Gdal.initialize();
    Dataset dataset = Gdal.open("/data/gebco_2024_web_mercator.tif");
    System.out.println(Gdal.info(dataset, new InfoOptions()));
  }

}
