package org.apache.baremaps.geocoderosm;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

public class BlockGeocoderImporter implements Consumer<Block> {
  private final IndexWriter indexWriter;

  public BlockGeocoderImporter(IndexWriter indexWriter) {
    this.indexWriter = indexWriter;
  }

  @Override
  public void accept(Block block) {
    if (block instanceof DataBlock dataBlock) {
      var documents = Stream
          .concat(dataBlock.getDenseNodes().stream(), dataBlock.getNodes().stream())
          .filter(this::isNodeRelevantForGeoCoder)
          .map(new OSMNodeDocumentMapper());
      try {
        indexWriter.addDocuments((Iterable<Document>) documents::iterator);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public boolean isNodeRelevantForGeoCoder(Node node) {
    var tags = node.getTags();
    return tags.containsKey("population") || tags.containsKey("place");
  }
}
