package com.baremaps.core;

import com.baremaps.core.blob.BlobStore;
import java.nio.file.Path;
import javax.sql.DataSource;

public interface Context {

  Path directory();

  BlobStore blobStore();

  DataSource dataSource();

}
