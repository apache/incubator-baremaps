/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.config.mapping;

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
