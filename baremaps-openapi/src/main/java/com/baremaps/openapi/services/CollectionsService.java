package com.baremaps.openapi.services;

import com.baremaps.api.CollectionsApi;
import com.baremaps.model.Collection;
import com.baremaps.model.Collections;
import java.util.UUID;

public class CollectionsService implements CollectionsApi {

  @Override
  public void addCollection(Collection collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteCollection(UUID collectionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection getCollection(UUID collectionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collections getCollections() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateCollection(UUID collectionId, Collection collection) {
    throw new UnsupportedOperationException();
  }

}
