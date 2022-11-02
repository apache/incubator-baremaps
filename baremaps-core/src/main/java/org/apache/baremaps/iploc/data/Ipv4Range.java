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



import com.google.common.primitives.Bytes;
import java.math.BigInteger;
import java.util.Arrays;
import net.ripe.ipresource.IpResourceRange;
import net.ripe.ipresource.Ipv4Address;

/**
 * Represents an IP range from a start IP to an end IP stored into longs because Java and Sqlite do
 * not have unsigned 32 bits integers
 */
public class Ipv4Range {
  private final byte[] start;
  private final byte[] end;

  /**
   * Constructs an Ipv4 range from a String
   *
   * @param range the range in format "192.168.0.1 - 192.168.0.255" or "192.168.0.1/24"
   */
  public Ipv4Range(String range) {
    IpResourceRange ipResourceRange = IpResourceRange.parse(range);
    this.start = forceIpOn4Bytes(ipResourceRange.getStart().getValue().toByteArray());
    this.end = forceIpOn4Bytes(ipResourceRange.getEnd().getValue().toByteArray());
  }

  /**
   * Constructs an Ipv4 range from a start and end ips
   *
   * @param start
   * @param end
   */
  public Ipv4Range(byte[] start, byte[] end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Force an IP on 4 bytes. Because the RIPE uses BigInteger as a means to store IPs and
   * BigIntegers toByteArray() method returns byte arrays of variable size, we need to force it onto
   * 4 bytes. It might be less than 4 bytes when the first part of the address if 0.x.x.x. It might
   * be more than 4 bytes when the first bit in the address is 1 because BigInteger will prepend a
   * zero byte to prevent negative value in the two's-complement binary representation
   *
   * @param bytes
   * @return
   */
  private static byte[] forceIpOn4Bytes(byte[] bytes) {
    if (bytes.length > 4) {
      return Arrays.copyOfRange(bytes, bytes.length - 4, bytes.length);
    } else if (bytes.length < 4) {
      return Bytes.concat(new byte[4 - bytes.length], bytes);
    }
    return bytes;
  }

  /**
   * Format an IP from a byte array into a string We need to prepend a byte of value 0 to the byte
   * array so that the MSB is 0 in the two's-complement binary representation
   *
   * @param ip the IP in a byte array
   * @return teh formatted IP
   */
  public static String format(byte[] ip) {
    byte[] zero = new byte[] {0};
    return new Ipv4Address(new BigInteger(Bytes.concat(zero, ip)).longValue()).toString();
  }

  /**
   * Returns the first IP in the range
   *
   * @return the first IP in the range
   */
  public byte[] getStart() {
    return this.start;
  }

  /**
   * Returns the last IP in the range
   *
   * @return the last IP in the range
   */
  public byte[] getEnd() {
    return this.end;
  }

  @Override
  public String toString() {
    return "Ipv4Range{" + "start=" + format(start) + ", end=" + format(end) + '}';
  }
}
