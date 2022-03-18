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

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;

public final class Analyser {

  private Analyser() {}

  public static TokenStream applyLanguageStopWordsFilter(TokenStream stream) {
    try (EnglishAnalyzer english = new EnglishAnalyzer()) {
      stream = new StopFilter(stream, english.getStopwordSet());
    }
    try (FrenchAnalyzer french = new FrenchAnalyzer()) {
      stream = new StopFilter(stream, french.getStopwordSet());
    }
    try (GermanAnalyzer german = new GermanAnalyzer()) {
      stream = new StopFilter(stream, german.getStopwordSet());
    }
    return stream;
  }
}
