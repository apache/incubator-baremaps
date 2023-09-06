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

package org.apache.baremaps.geocoderosm;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.Element;
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
      var documents = Stream.of(
          dataBlock.getDenseNodes().stream(),
          dataBlock.getNodes().stream(),
          dataBlock.getWays().stream(),
          dataBlock.getRelations().stream())
          .flatMap(element -> element)
          .filter(this::isNodeRelevantForGeoCoder)
          .map(new OSMNodeDocumentMapper());
      try {
        indexWriter.addDocuments((Iterable<Document>) documents::iterator);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }


  public boolean isNodeRelevantForGeoCoder(Element element) {
    var tags = element.getTags();
    return tags.containsKey("population") || tags.containsKey("place");
  }
}
