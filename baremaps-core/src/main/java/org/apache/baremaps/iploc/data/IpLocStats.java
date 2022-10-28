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

package org.apache.baremaps.iploc.data;

/** Store stats related to the geneation of the IpLoc database from the RIR's files */
public class IpLocStats {
  private int insertedByAddressCount = 0;
  private int insertedByDescrCount = 0;
  private int insertedByCountryCount = 0;
  private int insertedByGeolocCount = 0;
  private int notInsertedCount = 0;
  private int insertedByCountryCodeCount = 0;

  public IpLocStats() {}

  public void incrementInsertedByAddressCount() {
    insertedByAddressCount++;
  }

  public void incrementInsertedByDescrCount() {
    insertedByDescrCount++;
  }

  public void incrementInsertedByCountryCount() {
    insertedByCountryCount++;
  }

  public void incrementInsertedByGeolocCount() {
    insertedByGeolocCount++;
  }

  public void incrementInsertedByCountryCodeCount() {
    insertedByCountryCodeCount++;
  }

  public void incrementNotInsertedCount() {
    notInsertedCount++;
  }

  public int getInsertedByAddressCount() {
    return insertedByAddressCount;
  }

  public int getInsertedByDescrCount() {
    return insertedByDescrCount;
  }

  public int getInsertedByCountryCount() {
    return insertedByCountryCount;
  }

  public int getInsertedByGeolocCount() {
    return insertedByGeolocCount;
  }

  public int getNotInsertedCount() {
    return notInsertedCount;
  }

  public int getInsertedByCountryCodeCount() {
    return insertedByCountryCodeCount;
  }
}
