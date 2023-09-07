package org.apache.baremaps.tilestore.pmtiles;

public class Header {

    private int specVersion;
    private long rootDirectoryOffset;
    private long rootDirectoryLength;
    private long jsonMetadataOffset;
    private long jsonMetadataLength;
    private long leafDirectoryOffset;
    private long leafDirectoryLength;
    private long tileDataOffset;
    private long tileDataLength;
    private long numAddressedTiles;
    private long numTileEntries;
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

    public Header(int specVersion, long rootDirectoryOffset, long rootDirectoryLength, long jsonMetadataOffset, long jsonMetadataLength, long leafDirectoryOffset, long leafDirectoryLength, long tileDataOffset, long tileDataLength, long numAddressedTiles, long numTileEntries, long numTileContents, boolean clustered, Compression internalCompression, Compression tileCompression, TileType tileType, int minZoom, int maxZoom, double minLon, double minLat, double maxLon, double maxLat, int centerZoom, double centerLon, double centerLat) {
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

    public void setRootDirectoryOffset(long rootDirectoryOffset) {
        this.rootDirectoryOffset = rootDirectoryOffset;
    }

    public long getRootDirectoryLength() {
        return rootDirectoryLength;
    }

    public void setRootDirectoryLength(long rootDirectoryLength) {
        this.rootDirectoryLength = rootDirectoryLength;
    }

    public long getJsonMetadataOffset() {
        return jsonMetadataOffset;
    }

    public void setJsonMetadataOffset(long jsonMetadataOffset) {
        this.jsonMetadataOffset = jsonMetadataOffset;
    }

    public long getJsonMetadataLength() {
        return jsonMetadataLength;
    }

    public void setJsonMetadataLength(long jsonMetadataLength) {
        this.jsonMetadataLength = jsonMetadataLength;
    }

    public long getLeafDirectoryOffset() {
        return leafDirectoryOffset;
    }

    public void setLeafDirectoryOffset(long leafDirectoryOffset) {
        this.leafDirectoryOffset = leafDirectoryOffset;
    }

    public long getLeafDirectoryLength() {
        return leafDirectoryLength;
    }

    public void setLeafDirectoryLength(long leafDirectoryLength) {
        this.leafDirectoryLength = leafDirectoryLength;
    }

    public long getTileDataOffset() {
        return tileDataOffset;
    }

    public void setTileDataOffset(long tileDataOffset) {
        this.tileDataOffset = tileDataOffset;
    }

    public long getTileDataLength() {
        return tileDataLength;
    }

    public void setTileDataLength(long tileDataLength) {
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
}
