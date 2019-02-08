package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.DataStoreException;
import org.apache.commons.dbcp2.PoolingDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostgisStore<K, V> implements DataStore<K, V> {

    private final PoolingDataSource pool;

    private final PostgisTable<K, V> postgisTable;

    public PostgisStore(PoolingDataSource pool, PostgisTable<K, V> postgisTable) {
        this.pool = pool;
        this.postgisTable = postgisTable;
    }

    @Override
    public void add(V value) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            postgisTable.insert(connection, value);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void addAll(Collection<V> values) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            for(V value : values) {
                postgisTable.insert(connection, value);
            }
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public V get(K id) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            return postgisTable.select(connection, id);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public List<V> getAll(List<K> ids) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            List<V> entities = new ArrayList<>();
            for (K id : ids) {
                entities.add(postgisTable.select(connection, id));
            }
            return entities;
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void delete(K id) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            postgisTable.delete(connection, id);
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void deleteAll(List<K> ids) throws DataStoreException {
        try (Connection connection = pool.getConnection()) {
            for (K id : ids) {
                postgisTable.delete(connection, id);
            }
        } catch (SQLException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void close() {
        
    }
}
