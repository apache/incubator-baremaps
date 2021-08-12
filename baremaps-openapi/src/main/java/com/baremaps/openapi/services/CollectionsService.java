package com.baremaps.openapi.services;

import com.baremaps.api.CollectionsApi;
import com.baremaps.model.Collection;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class CollectionsService implements CollectionsApi {

  @Override
  public Response addCollection(Collection collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response deleteCollection(UUID collectionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response getCollection(UUID collectionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response getCollections() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response updateCollection(UUID collectionId, Collection collection) {
    throw new UnsupportedOperationException();
  }

}
