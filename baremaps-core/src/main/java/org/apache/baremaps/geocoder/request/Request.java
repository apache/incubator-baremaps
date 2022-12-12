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

package org.apache.baremaps.geocoder.request;



import org.apache.baremaps.geocoder.utils.IsoCountriesUtils;

public class Request {

  private final String query;
  private final String countryCode;

  private final int limit;

  public Request(String query, int limit) {
    this.query = query;
    this.limit = limit;
    this.countryCode = null;
  }

  public Request(String query, int limit, String countryCode) {
    this.query = query;
    this.limit = limit;
    if (IsoCountriesUtils.containsCountry(countryCode)) {
      this.countryCode = countryCode;
    } else {
      this.countryCode = null;
    }
  }

  public String query() {
    return query;
  }

  public String countryCode() {
    return countryCode;
  }

  public int limit() {
    return limit;
  }
}
