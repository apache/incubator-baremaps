package io.gazetteer.osm.store;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

public interface StoreWriter<K, V> {

  void put(K key, V values);

  void putAll(List<StoreEntry<K, V>> entries);

  void delete(K key);

  void deleteAll(List<K> keys);

  void importAll(List<StoreEntry<K, V>> values);

  default byte[] serialize(Geometry geometry) {
    if (geometry == null) {
      return null;
    }
    WKBWriter writer = new WKBWriter(2, wkbNDR, true);
    return writer.write(geometry);
  }
}
