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

package org.apache.baremaps.iploc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.baremaps.iploc.data.Ipv4Range;
import org.junit.jupiter.api.Test;

/**
 * Test the IP Range class which can generate 32 bits byte arrays representing IPv4 addresses from a
 * string
 */
class Ipv4RangeTest {

  @Test
  void testRange() {
    Ipv4Range ipv4Range = new Ipv4Range("0.0.0.0 - 0.0.0.255");
    assertArrayEquals(ipv4Range.getStart(), new byte[] {0x0, 0x0, 0x0, 0x0});
    assertArrayEquals(ipv4Range.getEnd(), new byte[] {0x0, 0x0, 0x0, (byte) 0xFF});
  }

  @Test
  void testRangeWithMask() {
    Ipv4Range ipv4Range = new Ipv4Range("0.0.0.0/24");
    assertArrayEquals(ipv4Range.getStart(), new byte[] {0x0, 0x0, 0x0, 0x0});
    assertArrayEquals(ipv4Range.getEnd(), new byte[] {0x0, 0x0, 0x0, (byte) 0xFF});
  }

  @Test
  void testRangeMaxValue() {
    Ipv4Range ipv4Range = new Ipv4Range("255.255.255.0 - 255.255.255.255");
    assertArrayEquals(ipv4Range.getStart(),
        new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0});
    assertArrayEquals(ipv4Range.getEnd(),
        new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
  }

  @Test
  void testRangeWithMaxMask() {
    Ipv4Range ipv4Range = new Ipv4Range("255.255.255.0/24");
    assertArrayEquals(ipv4Range.getStart(),
        new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0});
    assertArrayEquals(ipv4Range.getEnd(),
        new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
  }
}
