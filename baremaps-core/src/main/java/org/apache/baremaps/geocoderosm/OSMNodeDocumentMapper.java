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
    document.add(new TextField("name", node.getTags().get("name").toString(), Field.Store.YES));
    document.add(new StoredField("latitude", node.getLat()));
    document.add(new StoredField("longitude", node.getLon()));
    document.add(new NumericDocValuesField("population", Long.parseLong(node.getTags().get("population").toString())));
    document.add(new StoredField("population", Long.parseLong(node.getTags().get("population").toString())));
    return document;
  }
}
