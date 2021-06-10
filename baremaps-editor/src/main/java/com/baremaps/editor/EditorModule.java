package com.baremaps.editor;

import com.baremaps.config.BlobMapper;
import com.baremaps.tile.TileStore;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.function.Supplier;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class EditorModule implements Feature {

  private URI tileset;

  private URI style;

  private BlobMapper configStore;

  private Supplier<TileStore> tileStoreSupplier;

  public EditorModule(URI tileset, URI style, BlobMapper configStore, Supplier<TileStore> tileStoreSupplier) {
    this.tileset = tileset;
    this.style = style;
    this.configStore = configStore;
    this.tileStoreSupplier = tileStoreSupplier;
  }

  @Override
  public boolean configure(FeatureContext context) {
    context.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(tileset).named("tileset").to(URI.class);
        bind(style).named("style").to(URI.class);
        bind(configStore).to(BlobMapper.class);
        bind(tileStoreSupplier).to(new TypeLiteral<Supplier<TileStore>>(){});
      }
    });
    return true;
  }
}
