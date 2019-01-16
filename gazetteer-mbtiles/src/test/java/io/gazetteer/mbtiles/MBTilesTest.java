package io.gazetteer.mbtiles;

import org.junit.Assert;
import org.junit.Test;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MBTilesTest {

    @Test
    public void createDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        MBTiles.createDatabase(connection);
        DatabaseMetaData md = connection.getMetaData();
        ResultSet rs1 = md.getTables(null, null, "metadata", null);
        rs1.next();
        Assert.assertTrue(rs1.getRow() > 0);
        ResultSet rs2 = md.getTables(null, null, "tiles", null);
        rs2.next();
        Assert.assertTrue(rs2.getRow() > 0);
    }

    @Test
    public void getMetadata() throws SQLException {
        String database = getClass().getClassLoader().getResource("schema.db").getPath();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        Map<String, String> metadata = MBTiles.getMetadata(connection);
        Assert.assertTrue(metadata.size() > 0);
    }

    @Test
    public void getTile() throws SQLException {
        String database = getClass().getClassLoader().getResource("schema.db").getPath();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        Tile t1 = MBTiles.getTile(connection, new XYZ(0, 0, 0));
        Assert.assertNotNull(t1);
        Tile t2 = MBTiles.getTile(connection, new XYZ(1, 1, 1));
        Assert.assertNull(t2);
    }

    @Test
    public void setMetadata() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        MBTiles.createDatabase(connection);
        Map<String, String> m1 = new HashMap<>();
        m1.put("test", "test");
        MBTiles.setMetadata(connection, m1);
        Map<String, String> m2 = MBTiles.getMetadata(connection);
        Assert.assertTrue(m2.size() > 0);
    }

    @Test
    public void setTile() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        MBTiles.createDatabase(connection);
        MBTiles.setTile(connection, new XYZ(0, 0, 0), new Tile("test".getBytes()));
        Assert.assertNotNull(MBTiles.getTile(connection, new XYZ(0, 0, 0)));
    }
}