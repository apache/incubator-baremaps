/*
 * Copyright (C) 2020 The Baremaps Authors
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

package com.baremaps.osm.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.PrimitiveGroup;
import com.baremaps.osm.model.Member;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PrimitiveBlock {

  public final Osmformat.PrimitiveBlock primitiveBlock;
  private final int granularity;
  private final int dateGranularity;
  private final long latOffset;
  private final long lonOffset;
  private final String[] stringTable;

  public PrimitiveBlock(Osmformat.PrimitiveBlock primitiveBlock) {
    checkNotNull(primitiveBlock);
    this.primitiveBlock = primitiveBlock;
    this.granularity = primitiveBlock.getGranularity();
    this.latOffset = primitiveBlock.getLatOffset();
    this.lonOffset = primitiveBlock.getLonOffset();
    this.dateGranularity = primitiveBlock.getDateGranularity();
    this.stringTable = new String[primitiveBlock.getStringtable().getSCount()];
    for (int i = 0; i < stringTable.length; i++) {
      stringTable[i] = primitiveBlock.getStringtable().getS(i).toStringUtf8();
    }
  }

  public List<Node> getDenseNodes() {
    return primitiveBlock.getPrimitivegroupList()
        .stream()
        .flatMap(group -> readDenseNodes(group.getDense()))
        .collect(Collectors.toList());
  }

  private Stream<Node> readDenseNodes(Osmformat.DenseNodes input) {
    List<Node> nodes = new ArrayList<>();
    long id = 0;
    long lat = 0;
    long lon = 0;
    long timestamp = 0;
    long changeset = 0;
    int sid = 0;
    int uid = 0;

    // Index into the keysvals array.
    int j = 0;
    for (int i = 0; i < input.getIdCount(); i++) {
      id = input.getId(i) + id;

      Osmformat.DenseInfo info = input.getDenseinfo();
      int version = info.getVersion(i);
      uid = info.getUid(i) + uid;
      sid = info.getUserSid(i) + sid;
      timestamp = info.getTimestamp(i) + timestamp;
      changeset = info.getChangeset(i) + changeset;
      lat = input.getLat(i) + lat;
      lon = input.getLon(i) + lon;

      // If empty, assume that nothing here has keys or vals.
      Map<String, String> tags = new HashMap<>();
      if (input.getKeysValsCount() > 0) {
        while (input.getKeysVals(j) != 0) {
          int keyid = input.getKeysVals(j++);
          int valid = input.getKeysVals(j++);
          tags.put(getString(keyid), getString(valid));
        }
        j++; // Skip over the '0' delimiter.
      }

      nodes.add(
          new Node(id, version, getTimestamp(timestamp), changeset, uid, tags, getLon(lon), getLat(lat)));
    }
    return nodes.stream();
  }

  public List<Node> getNodes() {
    return primitiveBlock.getPrimitivegroupList()
        .stream()
        .flatMap(group -> readNodes(group.getNodesList()))
        .collect(Collectors.toList());
  }

  private Stream<Node> readNodes(List<Osmformat.Node> input) {
    List<Node> nodes = new ArrayList<>();
    for (Osmformat.Node node : input) {
      long id = node.getId();
      int version = node.getInfo().getVersion();
      LocalDateTime timestamp = getTimestamp(node.getInfo().getTimestamp());
      long changeset = node.getInfo().getChangeset();
      int uid = node.getInfo().getUid();
      Map<String, String> tags = new HashMap<>();
      for (int t = 0; t < node.getKeysList().size(); t++) {
        tags.put(getString(node.getKeysList().get(t)), getString(node.getKeysList().get(t)));
      }
      double lon = getLon(node.getLon());
      double lat = getLat(node.getLat());
      nodes.add(new Node(id, version, timestamp, changeset, uid, tags, lon, lat));
    }
    return nodes.stream();
  }

  public List<Way> getWays() {
    List<Way> ways = new ArrayList<>();
    for (PrimitiveGroup group : primitiveBlock.getPrimitivegroupList()) {
      for (Osmformat.Way way : group.getWaysList()) {
        long id = way.getId();
        int version = way.getInfo().getVersion();
        LocalDateTime timestamp = getTimestamp(way.getInfo().getTimestamp());
        long changeset = way.getInfo().getChangeset();
        int uid = way.getInfo().getUid();
        Map<String, String> tags = getTags(way.getKeysList(), way.getValsList());

        long nid = 0;
        List<Long> nodes = new ArrayList<>();
        for (int index = 0; index < way.getRefsCount(); index++) {
          nid = nid + way.getRefs(index);
          nodes.add(nid);
        }

        ways.add(new Way(id, version, timestamp, changeset, uid, tags, nodes));
      }
    }
    return ways;
  }

  public List<Relation> getRelations() {
    return primitiveBlock.getPrimitivegroupList()
        .stream()
        .flatMap(group -> readRelations(group.getRelationsList()))
        .collect(Collectors.toList());
  }

  protected Stream<Relation> readRelations(List<Osmformat.Relation> input) {
    List<Relation> relations = new ArrayList<>();
    for (Osmformat.Relation relation : input) {
      long id = relation.getId();
      int version = relation.getInfo().getVersion();
      LocalDateTime timestamp = getTimestamp(relation.getInfo().getTimestamp());
      long changeset = relation.getInfo().getChangeset();
      int uid = relation.getInfo().getUid();
      Map<String, String> tags = getTags(relation.getKeysList(), relation.getValsList());

      long mid = 0;
      List<Member> members = new ArrayList<>();
      for (int j = 0; j < relation.getMemidsCount(); j++) {
        mid = mid + relation.getMemids(j);
        String role = getString(relation.getRolesSid(j));
        String type = type(relation.getTypes(j));
        members.add(new Member(mid, type, role));
      }
      relations.add(new Relation(id, version, timestamp, changeset, uid, tags, members));
    }
    return relations.stream();
  }

  protected String type(Osmformat.Relation.MemberType type) {
    return type.name();
  }

  protected double getLat(long lat) {
    return (granularity * lat + latOffset) * .000000001;
  }

  protected double getLon(long lon) {
    return (granularity * lon + lonOffset) * .000000001;
  }

  protected LocalDateTime getTimestamp(long timestamp) {
    return LocalDateTime
        .ofInstant(Instant.ofEpochMilli(dateGranularity * timestamp), TimeZone.getDefault().toZoneId());
  }

  protected Map<String, String> getTags(List<Integer> keys, List<Integer> vals) {
    Map<String, String> tags = new HashMap<>();
    for (int t = 0; t < keys.size(); t++) {
      tags.put(getString(keys.get(t)), getString(vals.get(t)));
    }
    return tags;
  }

  protected String getString(int id) {
    return stringTable[id];
  }

}
