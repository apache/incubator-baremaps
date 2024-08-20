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
    org.gdal.gdal.Dataset dataset = gdal.OpenEx(path);
    return new Dataset(dataset);
  }

  public static Dataset copy(String path, Dataset source) {
    org.gdal.gdal.Driver driver = gdal.IdentifyDriver(path);
    return new Dataset(driver.CreateCopy(path, source.dataset));
  }

  public static String info(Dataset dataset) {
    return org.gdal.gdal.gdal.GDALInfo(dataset.dataset, null);
  }

  public static String info(Dataset dataset, InfoOptions options) {
    return org.gdal.gdal.gdal.GDALInfo(dataset.dataset,
        new org.gdal.gdal.InfoOptions(options.asVector()));
  }

  public static Dataset buildVRT(List<String> files, BuildVRTOptions options) {
    org.gdal.gdal.Dataset target = org.gdal.gdal.gdal.BuildVRT(
        "",
        new Vector<>(files),
        new org.gdal.gdal.BuildVRTOptions(options.asVector()));
    return new Dataset(target);
  }

  public static Dataset buildVRT(List<String> files, BuildVRTOptions options,
      ProgressListener progressListener) {
    ProgressCallback callback = new ProgressCallback(progressListener);
    try {
      org.gdal.gdal.Dataset target = org.gdal.gdal.gdal.BuildVRT(
          "",
          new Vector<>(files),
          new org.gdal.gdal.BuildVRTOptions(options.asVector()),
          callback);
      return new Dataset(target);
    } finally {
      callback.delete();
    }
  }

  public static Dataset translate(Dataset source, TranslateOptions options) {
    org.gdal.gdal.Dataset dataset = org.gdal.gdal.gdal.Translate(
        "",
        source.dataset,
        new org.gdal.gdal.TranslateOptions(options.asVector()));
    return new Dataset(dataset);
  }

  public static Dataset translate(Dataset source, TranslateOptions options,
      ProgressListener progressListener) {
    ProgressCallback callback = new ProgressCallback(progressListener);
    try {
      org.gdal.gdal.Dataset dataset = org.gdal.gdal.gdal.Translate(
          "",
          source.dataset,
          new org.gdal.gdal.TranslateOptions(options.asVector()),
          callback);
      return new Dataset(dataset);
    } finally {
      callback.delete();
    }
  }

  public static Dataset warp(Dataset source, WarpOptions options) {
    org.gdal.gdal.Dataset target = gdal.Warp(
        "",
        new org.gdal.gdal.Dataset[] {source.dataset},
        new org.gdal.gdal.WarpOptions(options.asVector()));
    return new Dataset(target);
  }

  public static Dataset warp(Dataset source, WarpOptions options,
      ProgressListener progressListener) {
    ProgressCallback callback = new ProgressCallback(progressListener);
    try {
      org.gdal.gdal.Dataset target = gdal.Warp(
          "",
          new org.gdal.gdal.Dataset[] {source.dataset},
          new org.gdal.gdal.WarpOptions(options.asVector()),
          callback);
      return new Dataset(target);
    } finally {
      callback.delete();
    }
  }

  public static void main(String[] args) {
    Gdal.initialize();
    Dataset dataset = Gdal.open("/data/gebco_2024_web_mercator.tif");
    System.out.println(Gdal.info(dataset, new InfoOptions()));

  }

}
