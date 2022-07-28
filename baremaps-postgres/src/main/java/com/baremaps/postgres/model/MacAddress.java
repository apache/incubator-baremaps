// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MacAddress {

    private final byte[] addressBytes;

    public MacAddress(byte[] addressBytes) {

        if(addressBytes == null) {
            throw new IllegalArgumentException("addressBytes");
        }

        if(addressBytes.length != 6) {
            throw new IllegalArgumentException("addressBytes");
        }

        this.addressBytes = addressBytes;
    }

    public byte[] getAddressBytes() {
        return addressBytes;
    }

    @Override
    public String toString() {

        List<String> bytesAsHexString = IntStream
                .range(0, addressBytes.length)
                .map(idx -> addressBytes[idx])
                .mapToObj(value -> String.format("0x%x", value))
                .collect(Collectors.toList());

        return String.join("-", bytesAsHexString);
    }
}
