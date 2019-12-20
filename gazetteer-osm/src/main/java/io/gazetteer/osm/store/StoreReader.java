package io.gazetteer.osm.store;

import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

public interface StoreReader<K, V> {

  V get(K key);

  List<V> getAll(List<K> keys);

  default Geometry deserialize(byte[] wkb) {
    try {
      WKBReader reader = new WKBReader(new GeometryFactory());
      return reader.read(wkb);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
