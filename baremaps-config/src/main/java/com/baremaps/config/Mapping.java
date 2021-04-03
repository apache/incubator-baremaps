package com.baremaps.config;

import com.baremaps.config.Bounds;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Mapping {

  @JsonProperty("allow-entities")
  private List<String> allowEntities;

  @JsonProperty("block-entities")
  private List<String> blockEntities;

  @JsonProperty("allow-tags")
  private List<String> allowTags;

  @JsonProperty("block-tags")
  private List<String> blockTags;

  private Bounds bounds;

  public List<String> getAllowTags() {
    return allowTags;
  }

  public void setAllowTags(List<String> allowTags) {
    this.allowTags = allowTags;
  }

  public List<String> getBlockTags() {
    return blockTags;
  }

  public void setBlockTags(List<String> blockTags) {
    this.blockTags = blockTags;
  }

}
