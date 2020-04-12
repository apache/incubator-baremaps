/*
 * Copyright (C) 2011 The Baremaps Authors
 *
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

package com.baremaps.osm.database;

import com.baremaps.osm.geometry.GeometryUtil;
import com.baremaps.osm.osmpbf.HeaderBlock;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;

public class HeaderTable {

  private static final String SELECT =
      "SELECT replication_timestamp, replication_sequence_number, replication_url, source, writing_program, st_asewkb(bbox) FROM osm_headers ORDER BY replication_timestamp DESC";

  private static final String INSERT =
      "INSERT INTO osm_headers (replication_timestamp, replication_sequence_number, replication_url, source, writing_program, bbox) VALUES (?, ?, ?, ?, ?, ?)";

  private final DataSource dataSource;

  public HeaderTable(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<HeaderBlock> select() throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      ResultSet result = statement.executeQuery();
      List<HeaderBlock> headerBlocks = new ArrayList<>();
      while (result.next()) {
        long replicationTimestamp = result.getLong(1);
        long replicationSequenceNumber = result.getLong(2);
        String replicationUrl = result.getString(3);
        String source = result.getString(4);
        String writingProgram = result.getString(5);
        Geometry bbox = GeometryUtil.deserialize(result.getBytes(6));
        headerBlocks.add(
            new HeaderBlock(
                replicationTimestamp,
                replicationSequenceNumber,
                replicationUrl,
                source,
                writingProgram,
                bbox));
      }
      return headerBlocks;
    }
  }

  public HeaderBlock getLast() throws SQLException {
    return select().get(0);
  }

  public void insert(HeaderBlock headerBlock) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, headerBlock.getReplicationTimestamp());
      statement.setLong(2, headerBlock.getReplicationSequenceNumber());
      statement.setString(3, headerBlock.getReplicationUrl());
      statement.setString(4, headerBlock.getSource());
      statement.setString(5, headerBlock.getWritingProgram());
      statement.setBytes(6, GeometryUtil.serialize(headerBlock.getBbox()));
      statement.execute();
    }
  }

}
