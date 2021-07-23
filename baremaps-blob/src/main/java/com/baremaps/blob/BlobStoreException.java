package com.baremaps.blob;

public class BlobStoreException extends Exception {

  public BlobStoreException(String message) {
    super(message);
  }

  public BlobStoreException(Throwable throwable) {
    super(throwable);
  }

}
