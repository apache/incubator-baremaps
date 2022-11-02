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



import net.ripe.ipresource.IpResourceRange;

/**
 * Represents an IP range from a start IP to an end IP stored into longs because Java and Sqlite do
 * not have unsigned 32 bits integers
 */
public class Ipv4Range {
  private final Ipv4 start;
  private final Ipv4 end;

  /**
   * Constructs an Ipv4 range from a String
   *
   * @param range the range in format "192.168.0.1 - 192.168.0.255" or "192.168.0.1/24"
   */
  public Ipv4Range(String range) {
    IpResourceRange ipResourceRange = IpResourceRange.parse(range);
    this.start = new Ipv4(ipResourceRange.getStart());
    this.end = new Ipv4(ipResourceRange.getEnd());
  }

  /**
   * Constructs an Ipv4 range from a start and end ips
   *
   * @param start
   * @param end
   */
  public Ipv4Range(byte[] start, byte[] end) {
    this.start = new Ipv4(start);
    this.end = new Ipv4(end);
  }

  /**
   * Returns the first IP in the range
   *
   * @return the first IP in the range
   */
  public byte[] getStart() {
    return this.start.getIp();
  }

  /**
   * Returns the last IP in the range
   *
   * @return the last IP in the range
   */
  public byte[] getEnd() {
    return this.end.getIp();
  }

  @Override
  public String toString() {
    return "Ipv4Range{" + "start=" + start.toString() + ", end=" + end.toString() + '}';
  }
}
