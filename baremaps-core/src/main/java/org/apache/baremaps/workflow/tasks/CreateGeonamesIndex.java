/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import org.apache.baremaps.geocoder.GeocoderConstants;
import org.apache.baremaps.geocoder.geonames.GeonamesDocumentMapper;
import org.apache.baremaps.geocoder.geonames.GeonamesReader;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task that creates a geonames index.
 */
public class CreateGeonamesIndex implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeonamesIndex.class);

  private Path dataFile;
  private Path indexDirectory;

  /**
   * Constructs a {@code CreateGeonamesIndex}.
   */
  public CreateGeonamesIndex() {
    // Default constructor
  }

  /**
   * Constructs a {@code CreateGeonamesIndex}.
   * 
   * @param dataFile the path to the data file
   * @param indexDirectory the path to the index directory
   */
  public CreateGeonamesIndex(Path dataFile, Path indexDirectory) {
    this.dataFile = dataFile;
    this.indexDirectory = indexDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws IOException {
    var directory = FSDirectory.open(indexDirectory);
    var config = new IndexWriterConfig(GeocoderConstants.ANALYZER);
    try (var indexWriter = new IndexWriter(directory, config);
        var inputStream = Files.newInputStream(dataFile)) {
      indexWriter.deleteAll();
      var documents = new GeonamesReader()
          .stream(inputStream)
          .map(new GeonamesDocumentMapper());
      indexWriter.addDocuments((Iterable<Document>) documents::iterator);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", CreateGeonamesIndex.class.getSimpleName() + "[", "]")
        .add("dataFile=" + dataFile)
        .add("indexDirectory=" + indexDirectory)
        .toString();
  }
}
