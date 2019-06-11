package io.gazetteer.osm.postgis;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgisUtil {

  public static PoolingDataSource createPoolingDataSource(String url) {
    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, null);
    PoolableConnectionFactory poolableConnectionFactory =
        new PoolableConnectionFactory(connectionFactory, null);
    ObjectPool<PoolableConnection> connectionPool =
        new GenericObjectPool<>(poolableConnectionFactory);
    poolableConnectionFactory.setPool(connectionPool);
    PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
    return dataSource;
  }



  public static void executeScript(Connection connection, String script) throws IOException, SQLException {
    URL url = Resources.getResource(script);
    String sql = Resources.toString(url, Charsets.UTF_8);
    connection.createStatement().execute(sql);
  }

}
