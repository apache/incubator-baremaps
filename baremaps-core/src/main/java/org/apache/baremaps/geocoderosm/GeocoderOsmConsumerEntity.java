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

package org.apache.baremaps.geocoderosm;

import java.util.function.Consumer;
import org.apache.baremaps.openstreetmap.model.Element;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeocoderOsmConsumerEntity implements Consumer<Entity> {
  private final IndexWriter indexWriter;
  private final GeocoderOsmDocumentMapper geocoderOsmDocumentMapper =
      new GeocoderOsmDocumentMapper();

  private static final Logger logger = LoggerFactory.getLogger(GeocoderOsmConsumerEntity.class);

  public GeocoderOsmConsumerEntity(IndexWriter indexWriter) {
    this.indexWriter = indexWriter;
  }

  @Override
  public void accept(Entity entity) {
    try {
      if (entity instanceof Element element) {
        var document = geocoderOsmDocumentMapper.apply(element);
        indexWriter.addDocument(document);
      }
    } catch (Exception e) {
      // Tolerate the failure of processing an element, partial data failure mode
      logger.warn("The following OSM entity ({}) is not processed due to {}", entity, e);
    }
  }
}
