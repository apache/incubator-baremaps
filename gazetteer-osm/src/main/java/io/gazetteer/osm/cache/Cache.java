package io.gazetteer.osm.cache;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.lmdbjava.DbiFlags.MDB_CREATE;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;
import org.locationtech.jts.geom.Coordinate;

public class Cache<T> {

  private final Env<ByteBuffer> env;

  private final Dbi<ByteBuffer> database;

  private final CacheMapper<T> mapper;

  public Cache(Env<ByteBuffer> env, Dbi<ByteBuffer> database, CacheMapper<T> mapper) {
    checkNotNull(env);
    checkNotNull(database);
    checkNotNull(mapper);
    this.env = env;
    this.database = database;
    this.mapper = mapper;
  }

  private ByteBuffer buffer(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(20);
    buffer.put(String.format("%020d", key).getBytes()).flip();
    return buffer.putLong(key);
  }

  public void put(Long key, T value) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      database.put(txn, buffer(key), mapper.write(value));
      txn.commit();
    }
  }

  public void putAll(List<Long> keys, List<T> values) {
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      for (int i = 0; i < keys.size(); i++) {
        database.put(txn, buffer(keys.get(i)), mapper.write(values.get(i)));
      }
      txn.commit();
    }
  }

  public T get(Long key) {
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      ByteBuffer buffer = database.get(txn, buffer(key));
      return mapper.read(buffer);
    }
  }

  public List<T> getAll(List<Long> keys) {
    List<T> list = new ArrayList<>();
    try (Txn<ByteBuffer> txn = env.txnRead()) {
      for (int i = 0; i < keys.size(); i++) {
        ByteBuffer buffer = database.get(txn, buffer(keys.get(i)));
        list.add(mapper.read(buffer));
      }
    }
    return list;
  }

  public void close() {
    database.close();
  }

  public static void main(String[] args) {
    Path lmdbPath = Paths.get("/tmp/lmdb");
    Env<ByteBuffer> env = Env.create().setMapSize(1_000_000_000L).setMaxDbs(3).open(lmdbPath.toFile());
    Cache<Coordinate> nodeCache = new Cache<>(env, env.openDbi("nodes", MDB_CREATE), new CoordinateMapper());
    nodeCache.put(1l, new Coordinate(1,1));
    System.out.println(nodeCache.get(1l));
  }

}
