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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

class GeocoderTest {

  private static final String v1 = "a simple text";
  private static final String v2 = "a simple test";

  @Test
  public void buildAndSearch() throws IOException, ParseException {
    Path path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    Geocoder geocoder = new Geocoder(path) {
      @Override
      protected Analyzer analyzer() {
        return new StandardAnalyzer();
      }

      @Override
      protected Stream<Document> documents() {
        Document d1 = new Document();
        d1.add(new Field("value", v1, TextField.TYPE_STORED));
        Document d2 = new Document();
        d2.add(new Field("value", v2, TextField.TYPE_STORED));
        return Stream.of(d1, d2);
      }

      @Override
      protected Query query(Analyzer analyzer, Request request) throws ParseException {
        return new QueryParser("value", analyzer).parse(request.query());
      }
    };
    geocoder.build();

    Response r3 = geocoder.search(new Request("simple", 10));
    assertEquals(2, r3.results().size());

    Response r1 = geocoder.search(new Request("text", 10));
    assertEquals(1, r1.results().size());
    assertEquals(v1, r1.results().get(0).document().get("value"));

    Response r2 = geocoder.search(new Request("test", 10));
    assertEquals(1, r2.results().size());
    assertEquals(v2, r2.results().get(0).document().get("value"));

    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
