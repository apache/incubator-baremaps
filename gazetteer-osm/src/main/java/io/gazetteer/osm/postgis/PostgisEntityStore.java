package io.gazetteer.osm.postgis;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import io.gazetteer.osm.model.Entity;
import io.gazetteer.osm.model.EntityStore;
import io.gazetteer.osm.model.EntityStoreException;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostgisEntityStore<E extends Entity> implements EntityStore<E> {

    private final PoolingDataSource pool;

    private final EntityTable<E> table;

    private final PgBulkInsert<E> bulkInsert;

    public PostgisEntityStore(PoolingDataSource pool, EntityTable<E> table, PgBulkInsert<E> bulkInsert) {
        this.pool = pool;
        this.table = table;
        this.bulkInsert = bulkInsert;
    }

    @Override
    public void add(E entity) throws EntityStoreException {
        try (Connection connection = pool.getConnection()) {
            table.insert(connection, entity);
        } catch (SQLException e) {
            throw new EntityStoreException(e);
        }
    }

    @Override
    public void addAll(Collection<E> entities) throws EntityStoreException {
        try (Connection connection = pool.getConnection()) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            bulkInsert.saveAll(pgConnection, entities);
        } catch (SQLException e) {
            throw new EntityStoreException(e);
        }
    }

    @Override
    public E get(long id) throws EntityStoreException {
        try (Connection connection = pool.getConnection()) {
            return table.select(connection, id);
        } catch (SQLException e) {
            throw new EntityStoreException(e);
        }
    }

    @Override
    public List<E> getAll(List<Long> ids) throws EntityStoreException {
        try (Connection connection = pool.getConnection()) {
            List<E> entities = new ArrayList<>();
            for (Long id : ids) {
                entities.add(table.select(connection, id));
            }
            return entities;
        } catch (SQLException e) {
            throw new EntityStoreException(e);
        }
    }

    @Override
    public void delete(long id) throws EntityStoreException {
        try (Connection connection = pool.getConnection()) {
            table.delete(connection, id);
        } catch (SQLException e) {
            throw new EntityStoreException(e);
        }
    }

    @Override
    public void deleteAll(List<Long> ids) throws EntityStoreException {
        try (Connection connection = pool.getConnection()) {
            for (Long id : ids) {
                table.delete(connection, id);
            }
        } catch (SQLException e) {
            throw new EntityStoreException(e);
        }
    }

    @Override
    public void close() throws Exception {
        
    }
}
