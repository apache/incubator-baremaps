package io.gazetteer.osm.model;

import java.util.Collection;
import java.util.List;

public interface EntityStore<E extends Entity> extends AutoCloseable {

    void add(E entity) throws EntityStoreException;

    void addAll(Collection<E> entities) throws EntityStoreException;

    E get(long id) throws EntityStoreException;

    List<E> getAll(List<Long> ids) throws EntityStoreException;

    void delete(long id) throws EntityStoreException;

    void deleteAll(List<Long> ids) throws EntityStoreException;

}
