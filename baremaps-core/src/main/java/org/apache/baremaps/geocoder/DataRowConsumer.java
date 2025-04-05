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

package org.apache.baremaps.geocoder;

import java.util.function.Consumer;
import org.apache.baremaps.calcite.DataRow;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRowConsumer implements Consumer<DataRow> {

  private static final Logger logger = LoggerFactory.getLogger(DataRowConsumer.class);

  private final IndexWriter indexWriter;

  private final DataRowMapper dataRowMapper = new DataRowMapper();

  public DataRowConsumer(IndexWriter indexWriter) {
    this.indexWriter = indexWriter;
  }

  @Override
  public void accept(DataRow row) {
    try {
      var document = dataRowMapper.apply(row);
      indexWriter.addDocument(document);
    } catch (Exception e) {
      logger.warn("The following row ({}) is not processed due to {}", row, e);
    }
  }
}
