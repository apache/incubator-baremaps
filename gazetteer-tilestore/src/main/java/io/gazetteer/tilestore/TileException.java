package io.gazetteer.tilestore;

public class TileException extends Exception {

  public TileException() {
    super();
  }

  public TileException(Exception e) {
    super(e);
  }

  public TileException(String m) {
    super(m);
  }

}
