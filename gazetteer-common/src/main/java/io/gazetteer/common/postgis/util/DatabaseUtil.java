package io.gazetteer.common.postgis.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DatabaseUtil {

  private static final String DATABASE_URL = "jdbc:postgresql://{0}:{1}/{2}?user={3}&password={4}&allowMultiQueries={5}";

  public static String url(String host, Integer port, String database, String user, String password, Boolean allowMultiQueries) {
    checkNotNull(host);
    checkNotNull(port);
    checkNotNull(database);
    checkNotNull(user);
    checkNotNull(password);
    checkNotNull(allowMultiQueries);
    return MessageFormat.format(DATABASE_URL, host, port, database, user, password, allowMultiQueries);
  }

  public static String url(String database, String user, String password) {
    return url("localhost", 5432, database, user, password, true);
  }

  public static PoolingDataSource poolingDataSource(String url) {
    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, null);
    PoolableConnectionFactory poolableConnectionFactory =
        new PoolableConnectionFactory(connectionFactory, null);
    ObjectPool<PoolableConnection> connectionPool =
        new GenericObjectPool<>(poolableConnectionFactory);
    poolableConnectionFactory.setPool(connectionPool);
    return new PoolingDataSource<>(connectionPool);
  }

  public static void executeScript(Connection connection, String script) throws IOException, SQLException {
    URL url = Resources.getResource(script);
    String sql = Resources.toString(url, Charsets.UTF_8);
    connection.createStatement().execute(sql);
  }

}
