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

package org.apache.baremaps.geocoder;



import java.util.List;
import org.apache.lucene.search.TopDocs;

public class Response {

  private final TopDocs topDocs;

  private final List<Result> results;

  public Response(TopDocs topDocs, List<Result> results) {
    this.topDocs = topDocs;
    this.results = results;
  }

  public TopDocs topDocs() {
    return topDocs;
  }

  public List<Result> results() {
    return results;
  }
}
