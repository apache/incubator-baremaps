package io.gazetteer.osm.osmpbf;

import com.google.protobuf.ByteString;
import org.openstreetmap.osmosis.osmbinary.Fileformat;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static com.google.common.base.Preconditions.checkNotNull;

public class PbfFileReader {

    private final DataInputStream input;

    public PbfFileReader(DataInputStream input) {
        checkNotNull(input);
        this.input = input;
    }

    public FileBlock read() throws IOException {
        // read the header
        int headerSize = input.readInt();
        byte[] headerData = new byte[headerSize];
        input.readFully(headerData);
        Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerData);

        // read the blob
        int blobSize = header.getDatasize();
        byte[] blobData = new byte[blobSize];
        input.readFully(blobData);
        Fileformat.Blob blob = Fileformat.Blob.parseFrom(blobData);

        // read the raw data
        if (blob.hasRaw()) {
            return new FileBlock(header.getType(), header.getIndexdata(), blob.getRaw());
        }

        // read the compressed zlib data
        if (blob.hasZlibData()) {
            byte[] raw = new byte[blob.getRawSize()];
            Inflater inflater = new Inflater();
            inflater.setInput(blob.getZlibData().toByteArray());
            try {
                inflater.inflate(raw);
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
            inflater.end();
            return new FileBlock(header.getType(), header.getIndexdata(), ByteString.copyFrom(raw));
        }

        throw new IOException("Unsupported data format");
    }

}
