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
package com.baremaps.openapi.services;

import com.baremaps.api.StylesApi;
import com.baremaps.model.MbStyle;
import com.baremaps.model.StyleSet;

public class StylesService implements StylesApi {

  @Override
  public void addStyle(MbStyle mbStyle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteStyle(String styleId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MbStyle getStyle(String styleId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StyleSet getStyleSet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateStyle(String styleId, MbStyle mbStyle) {
    throw new UnsupportedOperationException();
  }
}
