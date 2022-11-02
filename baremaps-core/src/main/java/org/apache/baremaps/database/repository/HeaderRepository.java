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

package org.apache.baremaps.database.repository;



import java.util.List;
import org.apache.baremaps.openstreetmap.model.Header;

/** Provides an interface to a repository storing OpenStreetMap headers. */
public interface HeaderRepository extends Repository<Long, Header> {

  /**
   * Selects all the headers.
   *
   * @throws RepositoryException
   */
  List<Header> selectAll() throws RepositoryException;

  /**
   * Selects the latest header.
   *
   * @throws RepositoryException
   */
  Header selectLatest() throws RepositoryException;
}
