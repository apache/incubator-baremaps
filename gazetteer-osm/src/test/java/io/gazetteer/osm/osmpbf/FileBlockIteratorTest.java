package io.gazetteer.osm.osmpbf;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static org.junit.jupiter.api.Assertions.*;

public class FileBlockIteratorTest {

  @Test
  public void next() throws FileNotFoundException {
    Iterator<FileBlock> reader = PBFUtil.iterator(osmPbfData());
    while (reader.hasNext()) {
      FileBlock block = reader.next();
      assertNotNull(block);
    }
    assertFalse(reader.hasNext());
  }

  @Test
  public void nextException() throws FileNotFoundException {
    assertThrows(NoSuchElementException.class, () -> {
      Iterator<FileBlock> reader = PBFUtil.iterator(osmPbfData());
      while (reader.hasNext()) {
        reader.next();
      }
      reader.next();
    });
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

}
