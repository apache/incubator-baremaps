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
