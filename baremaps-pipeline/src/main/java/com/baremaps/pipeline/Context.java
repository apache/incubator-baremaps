package com.baremaps.pipeline;

import com.baremaps.blob.BlobStore;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.geotoolkit.db.postgres.PostgresStore;

public interface Context {

   DataSource dataSource();

   BlobStore blobStore();

   PostgresStore postgresStore();

   Path directory();

   int srid();

}
