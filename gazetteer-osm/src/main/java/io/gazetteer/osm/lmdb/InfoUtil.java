package io.gazetteer.osm.lmdb;

import io.gazetteer.osm.model.Info;

public class InfoUtil {

  public static Lmdb.Info info(Info info) {
    return Lmdb.Info.newBuilder()
        .setId(info.getId())
        .setVersion(info.getVersion())
        .setTimestamp(info.getTimestamp())
        .setChangeset(info.getChangeset())
        .setUid(info.getUserId())
        .putAllTags(info.getTags())
        .build();
  }

  public static Info info(Lmdb.Info info) {
    return new Info(
        info.getId(),
        info.getVersion(),
        info.getTimestamp(),
        info.getChangeset(),
        info.getUid(),
        info.getTagsMap());
  }
}
