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
import java.util.List;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.geocoder.GeocoderConstants;
import org.apache.baremaps.geocoderosm.GeocoderOsmConsumerEntity;
import org.apache.baremaps.openstreetmap.pbf.PbfEntityReader;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental feature.
 *
 * @see org.apache.baremaps.geocoderosm
 */
public class CreateGeocoderOpenStreetMap implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeocoderOpenStreetMap.class);

  private Path file;
  private Path indexDirectory;

  /**
   * Constructs an {@code CreateGeocoderOpenStreetMap}.
   */
  public CreateGeocoderOpenStreetMap() {}

  /**
   * Constructs an {@code CreateGeocoderOpenStreetMap}.
   * 
   * @param file the OSM PBF file
   * @param indexDirectory the index directory
   */
  public CreateGeocoderOpenStreetMap(Path file, Path indexDirectory) {
    this.file = file;
    this.indexDirectory = indexDirectory;
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();

    var coordinateMap = context.getCoordinateMap(path);
    var referenceMap = context.getReferenceMap(path);

    var directory = FSDirectory.open(indexDirectory);
    var config = new IndexWriterConfig(GeocoderConstants.ANALYZER);

    try (var indexWriter = new IndexWriter(directory, config)) {
      var importer = new GeocoderOsmConsumerEntity(indexWriter);
      execute(
          path,
          coordinateMap,
          referenceMap,
          importer);
    }
  }

  public static void execute(
      Path path,
      DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap,
      GeocoderOsmConsumerEntity importer) throws IOException {

    // configure the block reader
    var reader = new PbfEntityReader()
        .geometries(true)
        // Must be to 4326 projection to avoid transformation before using Lucene API
        .projection(4326)
        .coordinateMap(coordinateMap)
        .referenceMap(referenceMap);

    try (var input = Files.newInputStream(path)) {
      StreamUtils.batch(reader.stream(input)).forEach(importer);
    }

  }
}
