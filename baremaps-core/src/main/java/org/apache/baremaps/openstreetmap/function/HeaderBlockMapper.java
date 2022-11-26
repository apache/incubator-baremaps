package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.Bound;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.HeaderBlock;

import java.util.function.Function;

/** A function that transforms a header block. */
public record HeaderBlockMapper(
  Function<Header, Header> headerMapper,
  Function<Bound, Bound> boundMapper
) implements Function<HeaderBlock, HeaderBlock> {

  /** {@inheritDoc} */
  @Override
  public HeaderBlock apply(HeaderBlock headerBlock) {
    return new HeaderBlock(headerMapper.apply(headerBlock.header()), boundMapper.apply(headerBlock.bound()));
  }
}
