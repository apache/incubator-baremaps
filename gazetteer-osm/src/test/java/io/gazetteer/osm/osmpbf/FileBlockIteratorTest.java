package io.gazetteer.osm.osmpbf;

import org.junit.Test;

public class FileBlockIteratorTest {

    @Test
    public void hasNext() {
    }

    @Test
    public void next() {
    }

    /*@Test
    public void writeBlock() throws IOException {
        DataInputStream input = new DataInputStream(new FileInputStream(new File("/home/bchapuis/Datasets/osm/switzerland-latest.osm.pbf")));
        DataOutputStream output =new DataOutputStream(new FileOutputStream(new File("/home/bchapuis/Projects/github.com/bchapuis/gazetteer/gazetteer-osm/src/test/resources/test.pbf")));

        boolean c = true;
        while (c) {
            // next the header
            int headerSize = input.readInt();
            byte[] headerData = new byte[headerSize];
            input.readFully(headerData);
            Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerData);

            // next the blob
            int blobSize = header.getDatasize();
            byte[] blobData = new byte[blobSize];
            input.readFully(blobData);
            Fileformat.Blob blob = Fileformat.Blob.parseFrom(blobData);

            byte[] raw = new byte[blob.getRawSize()];
            Inflater inflater = new Inflater();
            inflater.setInput(blob.getZlibData().toByteArray());
            try {
                inflater.inflate(raw);
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
            inflater.end();

            for (Osmformat.PrimitiveGroup g : Osmformat.DataBlock.parseFrom(raw).getPrimitivegroupList()) {
                if (g.getRelationsCount() > 0) {
                    c = false;
                }
            }

            if (!c) {
                output.writeInt(headerSize);
                output.write(headerData);
                output.write(blobData);
            }
        }
        output.close();
    }*/


//    @Test
//    public void testRead() throws IOException {
//        FileBlockIterator reader = (FileBlockIterator) PBFUtil.reader(OSM_PBF_DATA);
//        for (int i = 0; i < 10; i ++) {
//            FileBlock block = reader.read();
//            assertNotNull(block);
//        }
//    }

//    @Test(expected = EOFException.class)
//    public void testEOF() throws IOException {
//        FileBlockIterator reader = (FileBlockIterator) PBFUtil.reader(OSM_PBF_DATA);
//        for (int i = 0; i < 11; i ++) {
//            reader.read();
//        }
//    }

}