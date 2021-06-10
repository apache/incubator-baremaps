package com.baremaps.editor;

import com.baremaps.config.BlobMapper;
import com.baremaps.config.style.Style;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.TileStore;
import java.net.URI;
import java.util.function.Supplier;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ServerModule implements Feature {

  private Tileset tileset;

  private Style style;

  private TileStore tileStore;

  public ServerModule(Tileset tileset, Style style, TileStore tileStore) {
    this.tileset = tileset;
    this.style = style;
    this.tileStore = tileStore;
  }

  @Override
  public boolean configure(FeatureContext context) {
    context.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(tileset).to(Tileset.class);
        bind(style).to(Style.class);
        bind(tileStore).to(TileStore.class);
      }
    });
    return true;
  }
}
