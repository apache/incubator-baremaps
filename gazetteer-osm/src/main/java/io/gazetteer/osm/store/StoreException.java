package io.gazetteer.osm.store;

public class StoreException extends RuntimeException {

  public StoreException() {
    super();
  }

  public StoreException(Exception e) {
    super(e);
  }

}
