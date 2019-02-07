package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.DataStoreException;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostgisStore<K, V> implements DataStore<K, V> {

    private final PoolingDataSource pool;

    private final DataTable<K, V> dataTable;

    private final CopyManager<V> copyManager;

    public PostgisStore(PoolingDataSource pool, DataTable<K, V> dataTable, CopyManager<V> copyManager) {
        this.pool = pool;
        this.dataTable = dataTable;
        this.copyManager = copyManager;
    }

    @Override
    public void add(V value) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            dataTable.insert(connection, value);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void addAll(Collection<V> values) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            copyManager.saveAll(pgConnection, values);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public V get(K id) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            return dataTable.select(connection, id);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public List<V> getAll(List<K> ids) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            List<V> entities = new ArrayList<>();
            for (K id : ids) {
                entities.add(dataTable.select(connection, id));
            }
            return entities;
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void delete(K id) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            dataTable.delete(connection, id);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void deleteAll(List<K> ids) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            for (K id : ids) {
                dataTable.delete(connection, id);
            }
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void close() {
        
    }
}
