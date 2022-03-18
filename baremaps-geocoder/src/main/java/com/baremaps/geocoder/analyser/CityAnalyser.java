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

package com.baremaps.geocoder.analyser;

import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS;
import static org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter.GENERATE_WORD_PARTS;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class CityAnalyser extends Analyzer {

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    StandardTokenizer src = new StandardTokenizer();
    TokenStream result = new LowerCaseFilter(src);
    result = new ASCIIFoldingFilter(result);
    result =
        new WordDelimiterGraphFilter(result, GENERATE_NUMBER_PARTS | GENERATE_WORD_PARTS, null);
    result = new LengthFilter(result, 3, 40);
    result = Analyser.applyLanguageStopWordsFilter(result);
    return new TokenStreamComponents(src, result);
  }
}
