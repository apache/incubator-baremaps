// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

public class CollectionValueHandler<C, T extends Collection<C>> extends BaseValueHandler<T> {

    private final int oid;
    private final ValueHandler<C> valueHandler;

     public CollectionValueHandler(int oid, ValueHandler<C> valueHandler) {
         this.oid = oid;
         this.valueHandler = valueHandler;
     }

    @Override
    protected void internalHandle(DataOutputStream buffer, T value) throws IOException {
        ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
        DataOutputStream arrayOutput = new DataOutputStream(byteArrayOutput);

        arrayOutput.writeInt(1); // Dimensions, use 1 for one-dimensional arrays at the moment
        arrayOutput.writeInt(1); // The Array can contain Null Values
        arrayOutput.writeInt(oid); // Write the Values using the OID
        arrayOutput.writeInt(value.size()); // Write the number of elements
        arrayOutput.writeInt(1); // Ignore Lower Bound. Use PG Default for now

        // Now write the actual Collection elements using the inner handler:
        for (C element : value) {
            valueHandler.handle(arrayOutput, element);
        }

        buffer.writeInt(byteArrayOutput.size());
        buffer.write(byteArrayOutput.toByteArray());
    }
}
