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

package org.apache.baremaps.tilestore.pmtiles;

import java.util.Objects;

class Header {

  private int specVersion;
  private long rootDirectoryOffset;
  private long rootDirectoryLength;
  private long jsonMetadataOffset;
  private long jsonMetadataLength;
  private long leafDirectoryOffset;
  private long leafDirectoryLength;
  private long tileDataOffset;
  private long tileDataLength;

  /**
   * Number of Addressed Tiles
   * <p>
   * The Number of Addressed Tiles is an 8-byte field specifying the total number of tiles in the
   * PMTiles archive, before RunLength Encoding.
   * <p>
   * A value of 0 indicates that the number is unknown.
   * <p>
   * This field is encoded as a little-endian 64-bit unsigned integer.
   */
  private long numAddressedTiles;

  /**
   * Number of Tile Entries
   * <p>
   * The Number of Tile Entries is an 8-byte field specifying the total number of tile entries:
   * entries where RunLength is greater than 0.
   * <p>
   * A value of 0 indicates that the number is unknown.
   * <p>
   * This field is encoded as a little-endian 64-bit unsigned integer.
   *
   * @return the number of tile entries
   */
  private long numTileEntries;

  /**
   * Number of tile entries.
   * <p>
   * The Number of Tile Contents is an 8-byte field specifying the total number of blobs in the tile
   * data section.
   * <p>
   * A value of 0 indicates that the number is unknown.
   * <p>
   * This field is encoded as a little-endian 64-bit unsigned integer.
   */
  private long numTileContents;
  private boolean clustered;
  private Compression internalCompression;
  private Compression tileCompression;
  private TileType tileType;
  private int minZoom;
  private int maxZoom;
  private double minLon;
  private double minLat;
  private double maxLon;
  private double maxLat;
  private int centerZoom;
  private double centerLon;
  private double centerLat;

  public Header() {
    this.specVersion = 3;
  }

  public Header(
      int specVersion,
      long rootDirectoryOffset,
      long rootDirectoryLength,
      long jsonMetadataOffset,
      long jsonMetadataLength,
      long leafDirectoryOffset,
      long leafDirectoryLength,
      long tileDataOffset,
      long tileDataLength,
      long numAddressedTiles,
      long numTileEntries,
      long numTileContents,
      boolean clustered,
      Compression internalCompression,
      Compression tileCompression,
      TileType tileType,
      int minZoom,
      int maxZoom,
      double minLon,
      double minLat, double maxLon,
      double maxLat,
      int centerZoom,
      double centerLon,
      double centerLat) {
    this.specVersion = specVersion;
    this.rootDirectoryOffset = rootDirectoryOffset;
    this.rootDirectoryLength = rootDirectoryLength;
    this.jsonMetadataOffset = jsonMetadataOffset;
    this.jsonMetadataLength = jsonMetadataLength;
    this.leafDirectoryOffset = leafDirectoryOffset;
    this.leafDirectoryLength = leafDirectoryLength;
    this.tileDataOffset = tileDataOffset;
    this.tileDataLength = tileDataLength;
    this.numAddressedTiles = numAddressedTiles;
    this.numTileEntries = numTileEntries;
    this.numTileContents = numTileContents;
    this.clustered = clustered;
    this.internalCompression = internalCompression;
    this.tileCompression = tileCompression;
    this.tileType = tileType;
    this.minZoom = minZoom;
    this.maxZoom = maxZoom;
    this.minLon = minLon;
    this.minLat = minLat;
    this.maxLon = maxLon;
    this.maxLat = maxLat;
    this.centerZoom = centerZoom;
    this.centerLon = centerLon;
    this.centerLat = centerLat;
  }

  public int getSpecVersion() {
    return specVersion;
  }

  public void setSpecVersion(int specVersion) {
    this.specVersion = specVersion;
  }

  public long getRootDirectoryOffset() {
    return rootDirectoryOffset;
  }

  public void setRootOffset(long rootDirectoryOffset) {
    this.rootDirectoryOffset = rootDirectoryOffset;
  }

  public long getRootDirectoryLength() {
    return rootDirectoryLength;
  }

  public void setRootLength(long rootDirectoryLength) {
    this.rootDirectoryLength = rootDirectoryLength;
  }

  public long getJsonMetadataOffset() {
    return jsonMetadataOffset;
  }

  public void setMetadataOffset(long jsonMetadataOffset) {
    this.jsonMetadataOffset = jsonMetadataOffset;
  }

  public long getJsonMetadataLength() {
    return jsonMetadataLength;
  }

  public void setMetadataLength(long jsonMetadataLength) {
    this.jsonMetadataLength = jsonMetadataLength;
  }

  public long getLeafDirectoryOffset() {
    return leafDirectoryOffset;
  }

  public void setLeavesOffset(long leafDirectoryOffset) {
    this.leafDirectoryOffset = leafDirectoryOffset;
  }

  public long getLeafDirectoryLength() {
    return leafDirectoryLength;
  }

  public void setLeavesLength(long leafDirectoryLength) {
    this.leafDirectoryLength = leafDirectoryLength;
  }

  public long getTileDataOffset() {
    return tileDataOffset;
  }

  public void setTilesOffset(long tileDataOffset) {
    this.tileDataOffset = tileDataOffset;
  }

  public long getTileDataLength() {
    return tileDataLength;
  }

  public void setTilesLength(long tileDataLength) {
    this.tileDataLength = tileDataLength;
  }

  public long getNumAddressedTiles() {
    return numAddressedTiles;
  }

  public void setNumAddressedTiles(long numAddressedTiles) {
    this.numAddressedTiles = numAddressedTiles;
  }

  public long getNumTileEntries() {
    return numTileEntries;
  }

  public void setNumTileEntries(long numTileEntries) {
    this.numTileEntries = numTileEntries;
  }

  public long getNumTileContents() {
    return numTileContents;
  }

  public void setNumTileContents(long numTileContents) {
    this.numTileContents = numTileContents;
  }

  public boolean isClustered() {
    return clustered;
  }

  public void setClustered(boolean clustered) {
    this.clustered = clustered;
  }

  public Compression getInternalCompression() {
    return internalCompression;
  }

  public void setInternalCompression(Compression internalCompression) {
    this.internalCompression = internalCompression;
  }

  public Compression getTileCompression() {
    return tileCompression;
  }

  public void setTileCompression(Compression tileCompression) {
    this.tileCompression = tileCompression;
  }

  public TileType getTileType() {
    return tileType;
  }

  public void setTileType(TileType tileType) {
    this.tileType = tileType;
  }

  public int getMinZoom() {
    return minZoom;
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public int getMaxZoom() {
    return maxZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public double getMinLon() {
    return minLon;
  }

  public void setMinLon(double minLon) {
    this.minLon = minLon;
  }

  public double getMinLat() {
    return minLat;
  }

  public void setMinLat(double minLat) {
    this.minLat = minLat;
  }

  public double getMaxLon() {
    return maxLon;
  }

  public void setMaxLon(double maxLon) {
    this.maxLon = maxLon;
  }

  public double getMaxLat() {
    return maxLat;
  }

  public void setMaxLat(double maxLat) {
    this.maxLat = maxLat;
  }

  public int getCenterZoom() {
    return centerZoom;
  }

  public void setCenterZoom(int centerZoom) {
    this.centerZoom = centerZoom;
  }

  public double getCenterLon() {
    return centerLon;
  }

  public void setCenterLon(double centerLon) {
    this.centerLon = centerLon;
  }

  public double getCenterLat() {
    return centerLat;
  }

  public void setCenterLat(double centerLat) {
    this.centerLat = centerLat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Header header = (Header) o;
    return specVersion == header.specVersion && rootDirectoryOffset == header.rootDirectoryOffset
        && rootDirectoryLength == header.rootDirectoryLength
        && jsonMetadataOffset == header.jsonMetadataOffset
        && jsonMetadataLength == header.jsonMetadataLength
        && leafDirectoryOffset == header.leafDirectoryOffset
        && leafDirectoryLength == header.leafDirectoryLength
        && tileDataOffset == header.tileDataOffset && tileDataLength == header.tileDataLength
        && numAddressedTiles == header.numAddressedTiles && numTileEntries == header.numTileEntries
        && numTileContents == header.numTileContents && clustered == header.clustered
        && minZoom == header.minZoom && maxZoom == header.maxZoom
        && Double.compare(header.minLon, minLon) == 0 && Double.compare(header.minLat, minLat) == 0
        && Double.compare(header.maxLon, maxLon) == 0 && Double.compare(header.maxLat, maxLat) == 0
        && centerZoom == header.centerZoom && Double.compare(header.centerLon, centerLon) == 0
        && Double.compare(header.centerLat, centerLat) == 0
        && internalCompression == header.internalCompression
        && tileCompression == header.tileCompression && tileType == header.tileType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(specVersion, rootDirectoryOffset, rootDirectoryLength, jsonMetadataOffset,
        jsonMetadataLength, leafDirectoryOffset, leafDirectoryLength, tileDataOffset,
        tileDataLength, numAddressedTiles, numTileEntries, numTileContents, clustered,
        internalCompression, tileCompression, tileType, minZoom, maxZoom, minLon, minLat, maxLon,
        maxLat, centerZoom, centerLon, centerLat);
  }
}
