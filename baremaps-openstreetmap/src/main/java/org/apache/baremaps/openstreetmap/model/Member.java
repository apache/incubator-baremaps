/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.openstreetmap.model;



/** Represents a member of a relation in an OpenStreetMap dataset. */
public record Member(long ref, MemberType type, String role) {

  public enum MemberType {
    NODE,
    WAY,
    RELATION;

    public static MemberType forNumber(int value) {
      return switch (value) {
        case 0 -> NODE;
        case 1 -> WAY;
        case 2 -> RELATION;
        default -> throw new IllegalArgumentException();
      };
    }
  }
}
