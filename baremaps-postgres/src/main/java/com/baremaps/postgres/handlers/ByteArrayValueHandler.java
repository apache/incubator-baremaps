// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.DataOutputStream;
import java.io.IOException;

public class ByteArrayValueHandler extends BaseValueHandler<byte[]> {

	@Override
	protected void internalHandle(DataOutputStream buffer, final byte[] value) throws IOException {
		buffer.writeInt(value.length);
		buffer.write(value, 0, value.length);
	}
}
