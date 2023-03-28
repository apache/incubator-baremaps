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

package org.apache.baremaps.workflow.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.baremaps.geocoder.GeocoderConstants;
import org.apache.baremaps.geocoder.GeonamesDocumentMapper;
import org.apache.baremaps.geocoder.GeonamesReader;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task that creates a geonames index.
 */
public record CreateGeonamesIndex(Path dataFile, Path indexDirectory) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeonamesIndex.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var directory = MMapDirectory.open(indexDirectory);
    var config = new IndexWriterConfig(GeocoderConstants.ANALYZER);

    try (var indexWriter = new IndexWriter(directory, config);
        var inputStream = Files.newInputStream(dataFile)) {
      indexWriter.deleteAll();
      var documents = new GeonamesReader()
          .stream(inputStream)
          .map(new GeonamesDocumentMapper());
      indexWriter.addDocuments((Iterable<Document>) documents::iterator);
    } catch (IOException exception) {
      throw new RuntimeException();
    }
  }
}
