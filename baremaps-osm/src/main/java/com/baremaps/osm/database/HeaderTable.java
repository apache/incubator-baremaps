package com.baremaps.osm.database;

import com.baremaps.osm.domain.Header;
import java.util.List;

public interface HeaderTable extends EntityTable<Header> {

  List<Header> selectAll() throws DatabaseException;

  Header latest() throws DatabaseException;

}
