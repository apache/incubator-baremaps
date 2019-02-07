package io.gazetteer.osm.postgis;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import de.bytefish.pgbulkinsert.mapping.AbstractMapping;

public class CopyManager<E> extends PgBulkInsert<E> {

    public CopyManager(AbstractMapping<E> mapping) {
        super(mapping);
    }

}
