/*
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

package org.apache.baremaps.style;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A source is a collection of data that is used as a single input to style layers.
 *
 * @see <a href=
 *      "https://docs.mapbox.com/mapbox-gl-js/style-spec/sources/">https://docs.mapbox.com/mapbox-gl-js/style-spec/sources/</a>
 */
public class StyleSource {

  @JsonProperty("type")
  private String type;

  @JsonProperty("url")
  private String url;

  /**
   * Constructs a style source.
   */
  public StyleSource() {}

  /**
   * Constructs a style source.
   *
   * @param type the type of the source
   * @param url the url of the source
   */
  public StyleSource(String type, String url) {
    this.type = type;
    this.url = url;
  }

  /**
   * Returns the type of the source.
   *
   * @return the type of the source
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the url of the source.
   *
   * @return the url of the source
   */
  public StyleSource setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * Returns the url of the source.
   *
   * @return the url of the source
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url of the source.
   *
   * @param url the url of the source
   * @return the source
   */
  public StyleSource setUrl(String url) {
    this.url = url;
    return this;
  }
}
