package com.baremaps.openapi.services;

import com.baremaps.api.StylesApi;
import com.baremaps.model.Link;
import com.baremaps.model.MbStyle;
import com.baremaps.model.StyleSet;
import com.baremaps.model.StyleSetEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

public class StylesService implements StylesApi {

  private static final QualifiedType<MbStyle> MBSTYLE = QualifiedType.of(MbStyle.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public StylesService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void addStyle(MbStyle mbStyle) {
    UUID styleId = UUID.randomUUID(); // TODO: Read from body
    jdbi.useHandle(handle -> handle.createUpdate("insert into styles (id, style) values (:id, CAST(:json AS jsonb))")
        .bindByType("json", mbStyle, MBSTYLE)
        .bind("id", styleId)
        .execute());
  }

  @Override
  public void deleteStyle(UUID styleId) {
    jdbi.useHandle(handle -> handle.execute("delete from styles where id = (?)", styleId));
  }

  @Override
  public MbStyle getStyle(UUID styleId) {
    return jdbi.withHandle(handle ->
        handle.createQuery("select style from styles where id = :id")
            .bind("id", styleId)
            .mapTo(MBSTYLE)
            .one());
  }

  @Override
  public StyleSet getStyleSet() {
    List<UUID> ids = jdbi.withHandle(handle ->
        handle.createQuery("select id from styles")
            .mapTo(UUID.class)
            .list());

    StyleSet styleSet = new StyleSet();
    List<StyleSetEntry> entries = new ArrayList<>();

    for (UUID id : ids) {
      Link link = new Link();
      link.setHref(
          "http://localhost:8080/styles/" + id); // TODO: set dynamically from server or where the server gets it from
      link.setType("application/vnd.mapbox.style+json");
      link.setRel("stylesheet");

      StyleSetEntry entry = new StyleSetEntry();
      entry.setId(id);
      entry.setLinks(List.of(link));

      entries.add(entry);
    }

    styleSet.setStyles(entries);

    return styleSet;
  }

  @Override
  public void updateStyle(UUID styleId, MbStyle mbStyle) {
    jdbi.useHandle(handle -> handle.createUpdate("update styles set style = :json where id = :id")
        .bindByType("json", mbStyle, MBSTYLE)
        .bind("id", styleId)
        .execute());
  }
}
