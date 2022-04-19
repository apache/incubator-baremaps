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

package com.baremaps.iploc.data;

// TODO: this should be a package private class with non-static fields
public class IpLocStats {
  public static int inetnumInsertedByAddress = 0;
  public static int inetnumInsertedByDescr = 0;
  public static int inetnumInsertedByCountry = 0;
  public static int inetnumInsertedByGeoloc = 0;
  public static int inetnumNotInserted = 0;
  public static int inetnumInsertedByCountryCode = 0;
}
