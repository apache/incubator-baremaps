package org.apache.baremaps.tdtiles.subtree;

public record Subtree(Availability tileAvailability,
                      Availability contentAvailability,
                      Availability childSubtreeAvailability) {

}