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
import net.ripe.ipresource.Ipv4Address;
import net.ripe.ipresource.UniqueIpResource;

/**
 * Represents an IPv4
 */
public class Ipv4 {

  private final byte[] ip;

  /**
   * Constructs an Ipv4 range from a String
   *
   * @param ip the ip in format "192.168.0.1"
   */
  public Ipv4(String ip) {
    this(UniqueIpResource.parse(ip));
  }

  /**
   * Construct an Ipv4 from a UniqueIpResource
   *
   * @param uniqueIpResource
   */
  protected Ipv4(UniqueIpResource uniqueIpResource) {
    this.ip = forceIpOn4Bytes(uniqueIpResource.getValue().toByteArray());
  }

  /**
   * Constructs an Ipv4 range from an ip
   *
   * @param ip
   */
  public Ipv4(byte[] ip) {
    this.ip = ip;
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
   * Returns the IP
   *
   * @return the IP
   */
  public byte[] getIp() {
    return this.ip;
  }

  @Override
  public String toString() {
    return "Ipv4{" + "value=" + format(ip) + '}';
  }
}
