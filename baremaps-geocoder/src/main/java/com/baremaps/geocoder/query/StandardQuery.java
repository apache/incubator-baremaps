/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/heigvd-software-engineering/netscan/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.geocoder.query;

/**
 * Standard query used to find the best geolocation by street (5f*), housenumber (1f*), city(5f*),
 * postcode (3f*) The * is the weight of the attribute on the lucene query
 */
public class StandardQuery extends OsmQuery {

  public StandardQuery(String query) {
    super(5f, 1f, 5f, 3f, 1f, 1f, query);
  }
}
