package gazetteer.osm.database;

import de.bytefish.pgbulkinsert.pgsql.handlers.BaseValueHandler;
import mil.nga.sf.Geometry;
import mil.nga.sf.util.ByteWriter;
import mil.nga.sf.wkb.GeometryWriter;

import java.io.DataOutputStream;
import java.nio.ByteOrder;

public class GeometryValueHandler extends BaseValueHandler<Geometry> {

    @Override
    protected void internalHandle(DataOutputStream buffer, Geometry value) throws Exception {
        ByteWriter writer = new ByteWriter();
        writer.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        GeometryWriter.writeGeometry(writer, value);
        byte[] wkb = writer.getBytes();
        buffer.writeInt(wkb.length);
        buffer.write(wkb);
    }

}
