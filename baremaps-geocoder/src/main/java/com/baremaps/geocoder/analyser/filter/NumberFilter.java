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

package com.baremaps.geocoder.analyser.filter;

import java.io.IOException;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class NumberFilter extends FilteringTokenFilter {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  public NumberFilter(TokenStream in) {
    super(in);
  }

  @Override
  protected boolean accept() throws IOException {
    String token = new String(termAtt.buffer(), 0, termAtt.length());
    return !token.matches("[0-9,.]+");
  }
}
