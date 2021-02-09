package com.baremaps.config.mapping;

import com.baremaps.config.common.Bounds;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Mapping {

  @JsonProperty("allowed-entities")
  private List<String> allowedEntities;

  @JsonProperty("blocked-entities")
  private List<String> blockedEntities;

  @JsonProperty("allowed-tags")
  private List<String> allowedTags;

  @JsonProperty("blocked-tags")
  private List<String> blockedTags;

  private Bounds bounds;

  public List<String> getAllowedTags() {
    return allowedTags;
  }

  public void setAllowedTags(List<String> allowedTags) {
    this.allowedTags = allowedTags;
  }

  public List<String> getBlockedTags() {
    return blockedTags;
  }

  public void setBlockedTags(List<String> blockedTags) {
    this.blockedTags = blockedTags;
  }

}
