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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.baremaps.iploc.IpLocReader;
import org.apache.baremaps.iploc.IpLocRepository;
import org.apache.baremaps.stream.StreamException;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

public record CreateIplocIndex(
    Path geonamesIndexPath,
    List<Path> nicPaths,
    Path targetIplocIndexPath) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateIplocIndex.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    try (
        var directory = MMapDirectory.open(geonamesIndexPath);
        var searcherManager = new SearcherManager(directory, new SearcherFactory())) {

      logger.info("Creating the Iploc database");
      var jdbcUrl = String.format("JDBC:sqlite:%s", targetIplocIndexPath);

      var config = new SQLiteConfig();
      var dataSource = new SQLiteDataSource(config);
      dataSource.setUrl(jdbcUrl);

      var ipLocRepository = new IpLocRepository(dataSource);
      ipLocRepository.dropTable();
      ipLocRepository.createTable();
      ipLocRepository.createIndex();

      var ipLocReader = new IpLocReader(searcherManager);
      nicPaths.parallelStream().forEach(path -> {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
          var ipLocStream = ipLocReader.read(inputStream);
          ipLocRepository.save(ipLocStream);
        } catch (IOException e) {
          throw new StreamException(e);
        }
      });
    }
  }
}
