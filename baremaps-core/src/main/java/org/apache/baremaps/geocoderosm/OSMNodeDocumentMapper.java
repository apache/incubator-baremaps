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



import java.util.Optional;
import java.util.function.Function;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;


public class OSMNodeDocumentMapper implements Function<Node, Document> {

  @Override
  public Document apply(Node node) {
    Document document = new Document();
    document.add(new TextField(OSMTags.NAME.key(), node.getTags().get(OSMTags.NAME.key()).toString(), Field.Store.YES));
    document.add(new StoredField(OSMTags.LATITUDE.key(), node.getLat()));
    document.add(new StoredField(OSMTags.LONGITUDE.key(), node.getLon()));
    if (node.getTags().containsKey(OSMTags.POPULATION.key())) {
      var population = Long.parseLong(node.getTags().get(OSMTags.POPULATION.key()).toString());
      document.add(new NumericDocValuesField(OSMTags.POPULATION.key(), population));
      document.add(new StoredField(OSMTags.POPULATION.key(), population));
    }
    return document;
  }

}
