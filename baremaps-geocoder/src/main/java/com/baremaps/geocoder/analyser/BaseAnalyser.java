/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/heigvd-software-engineering/netscan/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.geocoder.analyser;

import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS;
import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.GENERATE_WORD_PARTS;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Cusutm Analyser for lucene Use: - StandardTokenizer - TokenStream - StopFilter(english stop
 * words, french stop words, german stop words)
 */
public class BaseAnalyser extends Analyzer {

  /** {@inheritDoc} */
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    StandardTokenizer src = new StandardTokenizer();
    TokenStream result = new LowerCaseFilter(src);
    result = new ASCIIFoldingFilter(result);
    result =
        new WordDelimiterGraphFilter(result, GENERATE_NUMBER_PARTS | GENERATE_WORD_PARTS, null);
    result = new GermanNormalizationFilter(result);
    result = Analyser.applyLanguageStopWordsFilter(result);

    return new TokenStreamComponents(src, result);
  }
}
