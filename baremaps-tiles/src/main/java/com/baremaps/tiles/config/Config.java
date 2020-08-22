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

package com.baremaps.tiles.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Config {

  private String id = "baremaps";

  private String version = "0.1";

  private String type = "baselayer";

  private String description = "";

  private String attribution = "";

  private String backgroundColor = "rgb(64, 92, 176)";

  private Server server = new Server();

  private Center center = new Center();

  private Bounds bounds = new Bounds();

  private List<Layer> layers = Lists.newArrayList();

  private List<Map<String, Object>> styles = Lists.newArrayList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Bounds getBounds() {
    return bounds;
  }

  public void setBounds(Bounds bounds) {
    this.bounds = bounds;
  }

  public Center getCenter() {
    return center;
  }

  public void setCenter(Center center) {
    this.center = center;
  }

  public List<Layer> getLayers() {
    return layers;
  }

  public void setLayers(List<Layer> layers) {
    this.layers = layers;
  }

  public List<Map<String, Object>> getStyles() {
    return styles;
  }

  public void setStyles(List<Map<String, Object>> styles) {
    this.styles = styles;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAttribution() {
    return attribution;
  }

  public void setAttribution(String attribution) {
    this.attribution = attribution;
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static Config load(byte[] input) throws IOException {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      Config config = mapper.readValue(input, Config.class);
      return config;
    } catch (MismatchedInputException e) {
      // return the default Config if the input is empty
      return new Config();
    }
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }
}
