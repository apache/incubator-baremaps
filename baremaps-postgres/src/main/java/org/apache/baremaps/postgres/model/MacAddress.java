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

package org.apache.baremaps.postgres.model;



import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MacAddress {

  private final byte[] addressBytes;

  public MacAddress(byte[] addressBytes) {

    if (addressBytes == null) {
      throw new IllegalArgumentException("addressBytes");
    }

    if (addressBytes.length != 6) {
      throw new IllegalArgumentException("addressBytes");
    }

    this.addressBytes = addressBytes;
  }

  public byte[] getAddressBytes() {
    return addressBytes;
  }

  @Override
  public String toString() {

    List<String> bytesAsHexString =
        IntStream.range(0, addressBytes.length).map(idx -> addressBytes[idx])
            .mapToObj(value -> String.format("0x%x", value)).collect(Collectors.toList());

    return String.join("-", bytesAsHexString);
  }
}
