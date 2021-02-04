package com.baremaps.osm.database;

import com.baremaps.osm.domain.Header;

public interface HeaderTable extends EntityTable<Header> {

  Header latest() throws DatabaseException;

}
