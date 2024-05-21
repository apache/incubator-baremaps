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

package org.apache.baremaps.openstreetmap.state;


import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.baremaps.openstreetmap.OpenStreetMap.Reader;
import org.apache.baremaps.openstreetmap.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for reading OSM state files. This code has been adapted from pyosmium (BSD 2-Clause
 * "Simplified" License).
 */
public class StateReader implements Reader<State> {

  private static final Logger logger = LoggerFactory.getLogger(StateReader.class);

  private final String replicationUrl;

  private final boolean balancedSearch;

  private final int retries;

  /**
   * Constructs a {@code StateReader}.
   */
  public StateReader() {
    this("https://planet.osm.org/replication/hour", true, 2);
  }

  /**
   * Constructs a {@code StateReader}.
   *
   * @param replicationUrl the replication URL
   * @param balancedSearch whether to use a balanced search
   */
  public StateReader(String replicationUrl, boolean balancedSearch) {
    this(replicationUrl, balancedSearch, 2);
  }

  /**
   * Constructs a {@code StateReader}.
   *
   * @param replicationUrl the replication URL
   * @param balancedSearch whether to use a balanced search
   * @param retries the number of retries
   */
  public StateReader(String replicationUrl, boolean balancedSearch, int retries) {
    this.replicationUrl = replicationUrl;
    this.balancedSearch = balancedSearch;
    this.retries = retries;
  }

  /**
   * Parse an OSM state file.
   *
   * @param input the OpenStreetMap state file
   * @return the state
   */
  @Override
  public State read(InputStream input) throws IOException {
    InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
    Map<String, String> map = new HashMap<>();
    for (String line : CharStreams.readLines(reader)) {
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    LocalDateTime timestamp = LocalDateTime.parse(map.get("timestamp").replace("\\", ""), format);
    return new State(sequenceNumber, timestamp);
  }

  /**
   * Get the state corresponding to the given timestamp.
   *
   * @param timestamp the timestamp
   * @return the state
   */
  public Optional<State> getStateFromTimestamp(LocalDateTime timestamp) {
    var upper = getState(Optional.empty());
    if (upper.isEmpty()) {
      return Optional.empty();
    }
    if (timestamp.isAfter(upper.get().getTimestamp()) || upper.get().getSequenceNumber() <= 0) {
      return upper;
    }
    var lower = Optional.<State>empty();
    var lowerId = Optional.of(0L);
    while (lower.isEmpty()) {
      lower = getState(lowerId);
      if (lower.isPresent() && lower.get().getTimestamp().isAfter(timestamp)) {
        if (lower.get().getSequenceNumber() == 0
            || lower.get().getSequenceNumber() + 1 >= upper.get().getSequenceNumber()) {
          return lower;
        }
        upper = lower;
        lower = Optional.empty();
        lowerId = Optional.of(0L);
      }
      if (lower.isEmpty()) {
        var newId = (lowerId.get() + upper.get().getSequenceNumber()) / 2;
        if (newId <= lowerId.get()) {
          return upper;
        }
        lowerId = Optional.of(newId);
      }
    }
    long baseSplitId;
    while (true) {
      if (balancedSearch) {
        baseSplitId = ((lower.get().getSequenceNumber() + upper.get().getSequenceNumber()) / 2);
      } else {
        var tsInt = upper.get().getTimestamp().toEpochSecond(ZoneOffset.UTC)
            - lower.get().getTimestamp().toEpochSecond(ZoneOffset.UTC);
        var seqInt = upper.get().getSequenceNumber() - lower.get().getSequenceNumber();
        var goal = timestamp.getSecond() - lower.get().getTimestamp().getSecond();
        baseSplitId = lower.get().getSequenceNumber() + (long) Math.ceil(goal * seqInt / tsInt);
        if (baseSplitId >= upper.get().getSequenceNumber()) {
          baseSplitId = upper.get().getSequenceNumber() - 1;
        }
      }
      var split = getState(Optional.of(baseSplitId));
      if (split.isEmpty()) {
        var splitId = baseSplitId - 1;
        while (split.isEmpty() && splitId > lower.get().getSequenceNumber()) {
          split = getState(Optional.of(splitId));
          splitId--;
        }
      }
      if (split.isEmpty()) {
        var splitId = baseSplitId + 1;
        while (split.isEmpty() && splitId < upper.get().getSequenceNumber()) {
          split = getState(Optional.of(splitId));
          splitId++;
        }
      }
      if (split.isEmpty()) {
        return lower;
      }
      if (split.get().getTimestamp().isBefore(timestamp)) {
        lower = split;
      } else {
        upper = split;
      }
      if (lower.get().getSequenceNumber() + 1 >= upper.get().getSequenceNumber()) {
        return lower;
      }
    }
  }

  /**
   * Get the state corresponding to the given sequence number.
   *
   * @param sequenceNumber the sequence number
   * @return the state
   */
  public Optional<State> getState(Optional<Long> sequenceNumber) {
    for (int i = 0; i < retries + 1; i++) {
      try (var inputStream = getStateUrl(sequenceNumber).openStream()) {
        var state = new StateReader().read(inputStream);
        return Optional.of(state);
      } catch (Exception e) {
        logger.error("Error while reading state file", e);
      }
    }
    return Optional.empty();
  }

  /**
   * Get the URL of the state file corresponding to the given sequence number.
   *
   * @param sequenceNumber the sequence number
   * @return the URL
   * @throws MalformedURLException if the URL is malformed
   */
  public URL getStateUrl(Optional<Long> sequenceNumber) throws MalformedURLException {
    if (sequenceNumber.isPresent()) {

      var s = String.format("%09d", sequenceNumber.get());
      var uri =
          String.format("%s/%s/%s/%s.%s", replicationUrl, s.substring(0, 3), s.substring(3, 6),
              s.substring(6, 9), "state.txt");
      return URI.create(uri).toURL();
    } else {
      return new URL(replicationUrl + "/state.txt");
    }
  }

  /**
   * Get the URL of a replication file.
   *
   * @param replicationUrl the replication URL
   * @param sequenceNumber the sequence number
   * @param extension the extension
   * @return the URL
   * @throws MalformedURLException if the URL is malformed
   */
  public URL getUrl(String replicationUrl, Long sequenceNumber, String extension)
      throws MalformedURLException {
    var s = String.format("%09d", sequenceNumber);
    var uri = String.format("%s/%s/%s/%s.%s", replicationUrl, s.substring(0, 3), s.substring(3, 6),
        s.substring(6, 9), extension);
    return URI.create(uri).toURL();
  }

}
